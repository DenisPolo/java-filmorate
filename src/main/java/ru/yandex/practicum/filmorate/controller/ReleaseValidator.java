package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

//класс обработки аннотации @ReleaseValidation
public class ReleaseValidator implements ConstraintValidator<ReleaseValidation, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date != null) {
            return date.isAfter(Film.START_RELEASE_DATE);
        }
        return false;
    }
}
