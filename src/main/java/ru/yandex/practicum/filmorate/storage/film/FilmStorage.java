package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.ResponseDefault;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface FilmStorage {

    Map<Integer, Film> getFilms();

    Film getFilm(Integer id) throws SQLException;

    Integer putFilm(Film film);

    boolean deleteFilm(Integer id);

    ResponseDefault addLike(Integer filmId, Integer userId);

    ResponseDefault removeLike(Integer filmId, Integer userId);

    List<Film> getPopular(Integer count);
}