package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

//класс обработки выбрасываемых при запросах исключений для вывода единого формата ответа
@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError methodArgumentNotValidExceptionHandle(MethodArgumentNotValidException e) {
        String defaultMessage;
        if (e.getMessage().contains("default message")) {
            List<String> messages = new ArrayList<>(List.of(e.getMessage().split(";")));
            defaultMessage = messages.get(messages.size() - 1).replaceAll(".*\\[|\\].*", "");
        } else {
            defaultMessage = e.getMessage();
        }
        log.warn(defaultMessage);
        return new ResponseError(defaultMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError runtimeExceptionHandle(RuntimeException e) {
        log.warn(e.getMessage());
        return new ResponseError(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AlreadyExistsException.class, Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError alreadyExistsExceptionHandle(Exception e) {
        log.warn(e.getMessage());
        return new ResponseError(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError notFoundExceptionHandle(NotFoundException e) {
        log.warn(e.getMessage());
        return new ResponseError(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}