package ru.yandex.practicum.filmorate.controller;

//класс DoesNotExistException для обработки апдейта несуществующего объекта
public class DoesNotExistException extends RuntimeException {
    public DoesNotExistException(String message) {
        super(message);
    }
}
