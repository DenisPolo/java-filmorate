package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.Constants;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

//класс обработки аннотации @ReleaseValidation
public class

ReleaseValidator implements ConstraintValidator<ReleaseValidation, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date != null) {
            return date.isAfter(Constants.START_RELEASE_DATE);
        }
        return false;
    }
}
