package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {

    Map<Integer, Film> getFilms();

    Film getFilm(Integer id);

    void putFilm(Film film);

    void deleteFilm(Integer id);
}
