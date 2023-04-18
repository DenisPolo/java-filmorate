package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    @Getter
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Map<Integer, Film> getFilms() {
        return films;
    }

    @Override
    public Film getFilm(Integer id) {
        return films.get(id);
    }

    @Override
    public void putFilm(Film film) {
        films.put(film.getId(), film);
    }

    public void deleteFilm(Integer id) {
        films.remove(id);
    }
}