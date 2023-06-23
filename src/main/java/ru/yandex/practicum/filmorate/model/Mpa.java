package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Mpa {
    Integer id;
    String name;

    public Mpa(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
