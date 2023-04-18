package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.ReleaseValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

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

    private Set<Long> likes;

    public boolean addLike(Integer userId) {
        if (likes == null) {
            likes = new TreeSet<>();
        }
        return likes.add(userId.longValue());
    }

    public boolean removeLike(Integer userId) {
        if (likes == null) {
            return false;
        }
        return likes.remove(userId.longValue());
    }

    public Integer getLikesAmount() {
        return (likes == null) ? null : likes.size();
    }
}