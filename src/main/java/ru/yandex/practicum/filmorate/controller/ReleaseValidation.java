package ru.yandex.practicum.filmorate.controller;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//интерфейс аннотации @ReleaseValidation
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = ReleaseValidator.class)
@Documented
public @interface ReleaseValidation {

    String message() default "{release.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}