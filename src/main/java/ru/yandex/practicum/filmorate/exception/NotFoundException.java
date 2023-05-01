package ru.yandex.practicum.filmorate.exception;

//класс DoesNotExistException для обработки апдейта несуществующего объекта
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
