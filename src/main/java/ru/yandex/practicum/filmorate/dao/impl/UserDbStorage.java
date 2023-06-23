package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ResponseDefault;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.yandex.practicum.filmorate.Constants.CONFIRMED_FRIENDSHIP;
import static ru.yandex.practicum.filmorate.Constants.NOT_CONFIRMED_FRIENDSHIP;

@Slf4j
@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    public final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, User> getUsers() {
        String sql = "SELECT  u.user_id,\n" +
                "       u.email,\n" +
                "       u.login,\n" +
                "       u.user_name,\n" +
                "       u.birthday,\n" +
                "       STRING_AGG (f.friend_id, ', ') AS friends\n" +
                "FROM users AS u\n" +
                "LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id\n" +
                "GROUP BY u.user_id\n" +
                "ORDER BY u.user_id ASC;";

        List<User> usersList = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));

        return usersList.stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    @Override
    public User getUser(Integer id) {
        checkUserExists(id);

        String sql = "SELECT  u.user_id,\n" +
                "       u.email,\n" +
                "       u.login,\n" +
                "       u.user_name,\n" +
                "       u.birthday,\n" +
                "       STRING_AGG (f.friend_id, ', ') AS friends\n" +
                "FROM users AS u\n" +
                "LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id\n" +
                "WHERE u.user_id = ?\n" +
                "GROUP BY u.user_id";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeUser(rs), id);
    }

    @Override
    public Integer putUser(User user) {
        checkUserMailAndLogin(user);
        checkUserName(user);

        if (user.getId() == null) {
            String sqlQuery = "INSERT INTO users(email, login, user_name, birthday) " +
                    "VALUES (?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getLogin());
                stmt.setString(3, user.getName());
                stmt.setDate(4, Date.valueOf(user.getBirthday()));
                return stmt;
            }, keyHolder);

            return keyHolder.getKey().intValue();
        } else {
            checkUserExists(user.getId());

            String sqlQuery = "UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ? WHERE user_id = ?";

            jdbcTemplate.update(sqlQuery
                    , user.getEmail()
                    , user.getLogin()
                    , user.getName()
                    , user.getBirthday()
                    , user.getId());

            return user.getId();
        }
    }

    @Override
    public boolean deleteUser(Integer id) {
        checkUserExists(id);
        String sqlQuery = "DELETE FROM users WHERE user_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public List<User> getUserFriends(Integer userId) {
        checkUserExists(userId);

        String sql = "SELECT  u.user_id,\n" +
                "       u.email,\n" +
                "       u.login,\n" +
                "       u.user_name,\n" +
                "       u.birthday,\n" +
                "       STRING_AGG (f.friend_id, ', ') AS friends\n" +
                "FROM users AS u\n" +
                "LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id\n" +
                "WHERE u.user_id IN (SELECT  friend_id AS user_id\n" +
                "       FROM friends\n" +
                "       WHERE user_id = ?)\n" +
                "GROUP BY u.user_id\n" +
                "ORDER BY u.user_id ASC;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), userId);
    }

    @Override
    public List<User> getCommonFriends(Integer firstId, Integer secondId) {
        checkUserExists(firstId);
        checkUserExists(secondId);

        String sql = "SELECT  u.user_id,\n" +
                "       u.email,\n" +
                "       u.login,\n" +
                "       u.user_name,\n" +
                "       u.birthday,\n" +
                "       STRING_AGG (f.friend_id, ', ') AS friends\n" +
                "FROM users AS u\n" +
                "LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id\n" +
                "WHERE u.user_id IN (SELECT  f1.friend_id\n" +
                "       FROM friends AS f1\n" +
                "       INNER JOIN (SELECT friend_id FROM friends WHERE user_id = ?) AS f2 ON f1.friend_id = f2.friend_id\n" +
                "       WHERE f1.user_id = ?)\n" +
                "GROUP BY u.user_id\n" +
                "ORDER BY u.user_id ASC;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), firstId, secondId);
    }

    @Override
    public ResponseDefault addFriend(Integer userId, Integer friendsId) {
        if (userId.equals(friendsId)) {
            throw new RuntimeException("Себя невозможно добавить в друзья ¯_(ツ)_/¯");
        }

        checkUserExists(userId);
        checkUserExists(friendsId);

        String sqlQuery = "SELECT friend_id FROM friends WHERE user_id = ? AND friend_id = ?";
        String sqlInsert = "INSERT INTO friends (user_id, friend_id, status_id, last_update) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        String sqlDelete = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

        if (jdbcTemplate.queryForRowSet(sqlQuery, userId, friendsId).next()) {
            throw new AlreadyExistsException("Пользователи с ID: " + userId + " и ID: " + friendsId + " уже друзья");
        } else if (!jdbcTemplate.queryForRowSet(sqlQuery, friendsId, userId).next()) {
            boolean updateResult = jdbcTemplate.update(sqlInsert
                    , userId
                    , friendsId
                    , NOT_CONFIRMED_FRIENDSHIP) > 0;
            if (updateResult) {
                String message = "Пользователь с ID: " + userId + " успешно отправил запрос на добавление в друзья" +
                        " пользователю с ID: " + friendsId + ", их дружба не подтверждена";
                log.info(message);
                return new ResponseDefault(message, HttpStatus.OK);
            } else {
                throw new AlreadyExistsException("Ошибка при обработке запроса добавления в друзья");
            }
        } else if (jdbcTemplate.update(sqlDelete, friendsId, userId) > 0) {
            boolean updateResultUser = jdbcTemplate.update(sqlInsert
                    , userId
                    , friendsId
                    , CONFIRMED_FRIENDSHIP) > 0;

            boolean updateResultFriend = jdbcTemplate.update(sqlInsert
                    , friendsId
                    , userId
                    , CONFIRMED_FRIENDSHIP) > 0;
            if (updateResultUser && updateResultFriend) {
                String message = "Пользователь с ID: " + userId + " успешно добавил в друзья пользователя с ID: "
                        + friendsId + ", теперь их дружба подтверждена";
                log.info(message);
                return new ResponseDefault(message, HttpStatus.OK);
            } else {
                throw new AlreadyExistsException("Ошибка при обработке запроса добавления в друзья");
            }
        } else {
            throw new AlreadyExistsException("Ошибка при обработке запроса добавления в друзья");
        }
    }

    @Override
    public ResponseDefault removeFriend(Integer userId, Integer friendsId) {
        if (userId.equals(friendsId)) {
            throw new RuntimeException("Всё! Я сам себе не друг! :(");
        }
        checkUserExists(userId);
        checkUserExists(friendsId);

        String sqlQuery = "SELECT friend_id FROM friends WHERE user_id = ? AND friend_id = ?";
        String sqlInsert = "INSERT INTO friends (user_id, friend_id, status_id, last_update) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        String sqlDelete = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

        if (!jdbcTemplate.queryForRowSet(sqlQuery, userId, friendsId).next()) {
            throw new NotFoundException("Пользователи с ID: " + userId + " и ID: " + friendsId
                    + " не являются друзьями");
        } else if (!jdbcTemplate.queryForRowSet(sqlQuery, friendsId, userId).next()) {
            boolean updateResult = jdbcTemplate.update(sqlDelete, userId, friendsId) > 0;
            if (updateResult) {
                String message = "Пользователь с ID: " + userId + " успешно отозвал запрос на добавление в друзья " +
                        "пользователю с ID: " + friendsId;
                log.info(message);
                return new ResponseDefault(message, HttpStatus.OK);
            } else {
                throw new AlreadyExistsException("Ошибка при обработке запроса удаления из друзей");
            }
        } else if (jdbcTemplate.update(sqlDelete, friendsId, userId) > 0) {
            boolean updateResultUser = jdbcTemplate.update(sqlDelete, userId, friendsId) > 0;

            boolean updateResultFriend = jdbcTemplate.update(sqlInsert
                    , friendsId
                    , userId
                    , NOT_CONFIRMED_FRIENDSHIP) > 0;
            if (updateResultUser && updateResultFriend) {
                String message = "Пользователь с ID: " + userId + " успешно удалил из друзей пользователя с ID: "
                        + friendsId;
                log.info(message);
                return new ResponseDefault(message, HttpStatus.OK);
            } else {
                throw new AlreadyExistsException("Ошибка при обработке запроса удаления из друзей");
            }
        } else {
            throw new AlreadyExistsException("Ошибка при обработке запроса удаления из друзей");
        }
    }

    private User makeUser(ResultSet rs) throws SQLException {
        System.out.println(rs);
        Integer id = rs.getInt("user_id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("user_name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        Set<Long> friends = ((rs.getString("friends") == null)) ? new TreeSet<>() :
                Stream.of(rs.getString("friends").split(", ")).map(Long::parseLong).collect(Collectors.toSet());

        return new User(id, email, login, name, birthday, friends);
    }

    private void checkUserExists(Integer id) {
        if (!jdbcTemplate.queryForRowSet("SELECT user_id FROM users WHERE user_id = ?", id).next()) {
            throw new NotFoundException("Пользователь с ID: " + id + " не существует");
        }
    }

    private void checkUserMailAndLogin(User user) {
        if ((user.getId() == null && jdbcTemplate.queryForRowSet("SELECT email FROM users WHERE email = ?"
                , user.getEmail()).next())
                || jdbcTemplate.queryForRowSet("SELECT email FROM users WHERE email = ? AND NOT user_id = ?"
                , user.getEmail(), user.getId()).next()) {
            throw new NotFoundException("Пользователь с email: " + user.getEmail() + " уже существует");
        }

        if ((user.getId() == null && jdbcTemplate.queryForRowSet("SELECT login FROM users WHERE login = ?"
                , user.getLogin()).next())
                || jdbcTemplate.queryForRowSet("SELECT login FROM users WHERE login = ? AND NOT user_id = ?"
                , user.getLogin(), user.getId()).next()) {
            throw new NotFoundException("Пользователь с login: " + user.getLogin() + " уже существует");
        }
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}