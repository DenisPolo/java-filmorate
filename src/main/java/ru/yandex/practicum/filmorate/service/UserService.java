package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        log.info("Запрос данных всех пользователей");
        return new ArrayList<>(userStorage.getUsers().values());
    }

    public User getUserById(Integer userId) {
        log.info("Запрос данных пользователя с ID: " + userId);
        return userStorage.getUser(userId);
    }

    public User createUser(User user) {
        Integer id = userStorage.putUser(user);
        log.info("Создан пользователь ID: " + id + ", email: " + user.getEmail() + ", login: "
                + user.getLogin());
        return userStorage.getUser(id);
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new RuntimeException("Переданные параметры пользователя не содержат ID");
        }

        Integer id = userStorage.putUser(user);
        log.info("Данные пользователя с ID: " + id + " обновлены email: " + user.getEmail()
                + ", login: " + user.getLogin());
        return userStorage.getUser(id);
    }

    public ResponseDefault deleteUser(Integer id) {
        if (userStorage.deleteUser(id)) {
            String message = "Пользователь с ID: " + id + " успешно удален";
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Ошибка при удалении пользователя");
        }
    }

    public List<User> getUserFriends(Integer userId) {
        log.info("Запрос друзей пользователя с ID: " + userId);
        return userStorage.getUserFriends(userId);
    }

    public List<User> getCommonFriends(Integer firstId, Integer secondId) {
        log.info("Запрос общих друзей пользователей с ID: " + firstId + " и ID: " + secondId);
        return userStorage.getCommonFriends(firstId, secondId);
    }

    public ResponseDefault addFriend(Integer userId, Integer friendsId) {
        log.info("Запрос добавления пользователя с ID: " + friendsId + " в друзья пользователея с ID: " + userId);
        return userStorage.addFriend(userId, friendsId);
    }

    public ResponseDefault removeFriend(Integer userId, Integer friendsId) {
        log.info("Запрос удаления пользователя с ID: " + friendsId + " из друзей пользователея с ID: " + userId);
        return userStorage.removeFriend(userId, friendsId);
    }
}