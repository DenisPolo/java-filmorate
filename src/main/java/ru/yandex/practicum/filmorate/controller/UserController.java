package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private int id;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping()
    public List<User> getAllUsers() {
        log.info("Запрос данных всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping()
    public User createUser(@Valid @RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        id++;
        user.setId(id);
        users.put(id, user);
        log.info("Создан пользователь ID: " + user.getId() + ", email: " + user.getEmail() + ", login: " + user.getLogin());
        return user;
    }

    @PutMapping()
    public User updateUser(@RequestBody User user) {
        try {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            if (users.containsKey(user.getId())) {
                users.put(user.getId(), user);
                log.info("Данные пользователя с ID: " + user.getId() + " обновлены email: " + user.getEmail()
                        + ", login: " + user.getLogin());
            } else {
                throw new DoesNotExistException("Пользователя с ID: " + user.getId() + " не существует");
            }
            return user;
        } catch (DoesNotExistException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}