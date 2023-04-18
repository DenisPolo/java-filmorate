package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    @Getter
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User getUser(Integer id) {
        return users.get(id);
    }

    @Override
    public void putUser(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void deleteUser(Integer id) {
        users.remove(id);
    }
}