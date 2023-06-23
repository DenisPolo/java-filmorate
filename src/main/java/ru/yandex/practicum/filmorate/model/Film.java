package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.ReleaseValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film {
    private Integer id;

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @ReleaseValidation(message = "Не верно указана дата релиза")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма не может быть отрицательной")
    private Integer duration;

    private Mpa mpa;

    private Set<Genre> genres;

    private Set<Long> likes;

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration, Mpa mpa,
                Set<Genre> genres, Set<Long> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
        this.likes = likes;
    }

    public Integer getLikesAmount() {
        return (likes == null) ? null : likes.size();
    }
}