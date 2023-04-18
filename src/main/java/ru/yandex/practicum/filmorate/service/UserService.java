package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private int id;
    private final UserStorage userStorage;
    private FilmStorage filmStorage;

    @Autowired
    public UserService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public List<User> getAllUsers() {
        log.info("Запрос данных всех пользователей");
        return new ArrayList<>(userStorage.getUsers().values());
    }

    public User getUserById(Integer userId) {
        checkUserExists(userId);
        log.info("Запрос данных пользователя с ID: " + userId);
        return userStorage.getUser(userId);
    }

    public User createUser(User user) {
        checkUserMailAndLogin(user);
        checkUserName(user);

        id++;
        user.setId(id);
        userStorage.putUser(user);
        log.info("Создан пользователь ID: " + user.getId() + ", email: " + user.getEmail() + ", login: "
                + user.getLogin());
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new RuntimeException("Переданные параметры пользователя не содержат ID");
        }

        checkUserExists(user.getId());
        checkUserMailAndLogin(user);
        checkUserName(user);

        userStorage.putUser(user);
        log.info("Данные пользователя с ID: " + user.getId() + " обновлены email: " + user.getEmail()
                + ", login: " + user.getLogin());
        return user;
    }

    public ResponseDefault deleteUser(Integer id) {
        checkUserExists(id);

        if (userStorage.getUsers().get(id).getFriends() != null) {
            for (Long friendId : userStorage.getUsers().get(id).getFriends()) {
                userStorage.getUser(friendId.intValue()).removeFriend(id);
            }
        }
        if (filmStorage.getFilms() != null) {
            for (Film film : filmStorage.getFilms().values()) {
                film.removeLike(id);
            }
        }
        userStorage.deleteUser(id);
        String message = "Пользователь с ID: " + id + " успешно удален";
        log.info(message);
        return new ResponseDefault(message, HttpStatus.OK);
    }

    public List<User> getUserFriends(Integer userId) {
        checkUserExists(userId);

        List<User> friends = new ArrayList<>();

        if (userStorage.getUser(userId).getFriends() == null) {
            return friends;
        }

        userStorage.getUser(userId)
                .getFriends()
                .forEach(id -> friends.add(userStorage.getUser(id.intValue())));
        log.info("Запрос друзей пользователя с ID: " + userId);
        return friends;
    }

    public List<User> getCommonFriends(Integer firstId, Integer secondId) {
        checkUserExists(firstId);
        checkUserExists(secondId);

        List<User> friends = new ArrayList<>();

        if (userStorage.getUser(firstId).getFriends() == null
                || userStorage.getUser(secondId).getFriends() == null) {
            log.info("Запрос общих друзей пользователей с ID: " + firstId + " и ID: " + secondId);
            return friends;
        }

        Set<Long> commonFriends = new TreeSet<>(userStorage.getUser(firstId).getFriends());

        commonFriends.retainAll(userStorage.getUser(secondId).getFriends());
        commonFriends.forEach(id -> friends.add(userStorage.getUser(id.intValue())));
        log.info("Запрос общих друзей пользователей с ID: " + firstId + " и ID: " + secondId);
        return friends;
    }

    public ResponseDefault addFriend(Integer userId, Integer friendsId) {
        checkUserExists(userId);
        checkUserExists(friendsId);

        if (userStorage.getUser(userId).addFriend(friendsId)
                && userStorage.getUser(friendsId).addFriend(userId)) {
            String message = "Пользователи с ID: " + userId + " и ID: " + friendsId + " успешно добавлены в друзья";
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Пользователи с ID: " + userId + " и ID: " + friendsId + " уже друзья");
        }
    }

    public ResponseDefault removeFriend(Integer userId, Integer friendsId) {
        checkUserExists(userId);
        checkUserExists(friendsId);

        if (userStorage.getUser(userId).removeFriend(friendsId)
                && userStorage.getUser(friendsId).removeFriend(userId)) {
            String message = "Пользователи с ID: " + userId + " и ID: " + friendsId + " больше не друзья :(";
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Пользователи с ID: " + userId + " и ID: " + friendsId + " не являются друзьями");
        }
    }

    private void checkUserExists(Integer userId) {
        if (!userStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не существует");
        }
    }

    private void checkUserMailAndLogin(User user) {
        if (user.getId() == null) {
            if (userStorage.getUsers().values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
                throw new AlreadyExistsException("Пользователь с email: " + user.getEmail() + " уже существует");
            }
            if (userStorage.getUsers().values().stream().anyMatch(u -> u.getLogin().equals(user.getLogin()))) {
                throw new AlreadyExistsException("Пользователь с login: " + user.getLogin() + " уже существует");
            }
        } else {
            for (User u : userStorage.getUsers().values()) {
                if (!Objects.equals(user.getId(), u.getId()) && user.getEmail().equals(u.getEmail())) {
                    throw new AlreadyExistsException("Пользователь с email: " + user.getEmail() + " уже существует");
                }
                if (!Objects.equals(user.getId(), u.getId()) && user.getLogin().equals(u.getLogin())) {
                    throw new AlreadyExistsException("Пользователь с login: " + user.getLogin() + " уже существует");
                }
            }
        }
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}