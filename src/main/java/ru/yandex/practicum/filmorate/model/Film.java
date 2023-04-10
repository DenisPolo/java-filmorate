package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.ReleaseValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class Film {
    public static final LocalDate START_RELEASE_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    public static final DateTimeFormatter RELEASE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int id;

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @ReleaseValidation(message = "Не верно указана дата релиза")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма не может быть отрицательной")
    private Integer duration;
}