package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ResponseDefault;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    Map<Integer, User> getUsers();

    User getUser(Integer id);

    Integer putUser(User user);

    boolean deleteUser(Integer id);

    List<User> getUserFriends(Integer userId);

    List<User> getCommonFriends(Integer firstId, Integer secondId);

    ResponseDefault addFriend(Integer userId, Integer friendsId);

    ResponseDefault removeFriend(Integer userId, Integer friendsId);
}