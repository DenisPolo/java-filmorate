package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.ResponseDefault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Qualifier("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private int id;

    @Getter
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Map<Integer, Film> getFilms() {
        return films;
    }

    @Override
    public Film getFilm(Integer id) {
        checkFilmExists(id);
        return films.get(id);
    }

    @Override
    public Integer putFilm(Film film) {
        checkFilmName(film);
        if (film.getId() == null) {
            id++;
            film.setId(id);
        }
        films.put(film.getId(), film);
        return id;
    }

    public boolean deleteFilm(Integer id) {
        films.remove(id);
        return !films.containsKey(id);
    }

    private void checkFilmExists(Integer filmId) {
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с ID: " + filmId + " не существует");
        }
    }

    private void checkFilmName(Film film) {
        if ((film.getId() == null)
                && films.values().stream().anyMatch(f -> f.getName().equals(film.getName()))) {
            throw new AlreadyExistsException("Фильм " + film.getName() + " уже существует");
        } else {
            for (Film f : films.values()) {
                if (!Objects.equals(film.getId(), f.getId()) && film.getName().equals(f.getName())) {
                    throw new AlreadyExistsException("Фильм " + film.getName() + " уже существует");
                }
            }
        }
    }

    @Override
    public ResponseDefault addLike(Integer filmId, Integer userId) {
        return null;
    }

    @Override
    public ResponseDefault removeLike(Integer filmId, Integer userId) {
        return null;
    }

    @Override
    public List<Film> getPopular(Integer count) {
        return null;
    }
}