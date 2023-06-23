package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ResponseDefault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    @Getter
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User getUser(Integer id) {
        return users.get(id);
    }

    @Override
    public Integer putUser(User user) {
        users.put(user.getId(), user);
        return user.getId();
    }

    @Override
    public boolean deleteUser(Integer id) {
        users.remove(id);
        return !users.containsKey(id);
    }

    @Override
    public List<User> getUserFriends(Integer userId) {
        return null;
    }

    @Override
    public List<User> getCommonFriends(Integer firstId, Integer secondId) {
        return null;
    }

    @Override
    public ResponseDefault addFriend(Integer userId, Integer friendsId) {
        return null;
    }

    @Override
    public ResponseDefault removeFriend(Integer userId, Integer friendsId) {
        return null;
    }
}