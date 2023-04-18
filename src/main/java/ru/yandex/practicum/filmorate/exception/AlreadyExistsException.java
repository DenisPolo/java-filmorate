package ru.yandex.practicum.filmorate.exception;

//класс AlreadyExistsException для обработки добавления нового объекта
public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
