package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

//класс ResponseError создан для единого формата ответа ошибки
@ToString
@Getter
public class ResponseError {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime time = LocalDateTime.now();
    private final HttpStatus status;
    private final String message;

    public ResponseError(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
