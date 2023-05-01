package ru.yandex.practicum.filmorate.service;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ResponseError;

public class ResponseDefault extends ResponseError {
    public ResponseDefault(String message, HttpStatus status) {
        super(message, status);
    }
}
