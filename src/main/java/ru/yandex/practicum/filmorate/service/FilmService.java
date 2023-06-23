package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getFilms() {
        log.info("Запрос списка всех фильмов");
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    public Film getFilm(Integer id) {
        log.info("Запрос фильма с ID: " + id);
        try {
            return filmStorage.getFilm(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Film addFilm(Film film) {
        Integer id = filmStorage.putFilm(film);
        try {
            log.info("Добавлен фильм id: " + id + ", name: " + film.getName() + ", release: "
                    + film.getReleaseDate().format(Constants.RELEASE_FORMATTER));
            return filmStorage.getFilm(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new RuntimeException("Переданные параметры фильма не содержат ID");
        }

        try {
            Integer id = filmStorage.putFilm(film);
            log.info("Данные фильма с ID: " + film.getId() + " обновлены name: " + film.getName() + ", release: "
                    + film.getReleaseDate().format(Constants.RELEASE_FORMATTER));
            return filmStorage.getFilm(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseDefault deleteFilm(Integer id) {
        if (filmStorage.deleteFilm(id)) {
            String message = "Фильм с ID: " + id + " успешно удален";
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Ошибка при удалении фильма");
        }
    }

    public ResponseDefault addLike(Integer filmId, Integer userId) {
        log.info("Запрос добавления лайка фильму с ID: " + filmId + " от пользователя с ID: " + userId);
        return filmStorage.addLike(filmId, userId);
    }

    public ResponseDefault removeLike(Integer filmId, Integer userId) {
        log.info("Запрос удаления лайка у фильма с ID: " + filmId + " от пользователя с ID: " + userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        log.info("Запрос " + count + " самых популярных фильмов");
        return filmStorage.getPopular(count);
    }
}