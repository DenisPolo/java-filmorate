package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private int id;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getFilms() {
        log.info("Запрос списка всех фильмов");
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    public Film getFilm(Integer id) {
        checkFilmExists(id);

        log.info("Запрос фильма с ID: " + id);
        return filmStorage.getFilm(id);
    }

    public Film addFilm(Film film) {
        checkFilmName(film);

        id++;
        film.setId(id);
        filmStorage.putFilm(film);
        log.info("Добавлен фильм id: " + id + ", name: " + film.getName() + ", release: "
                + film.getReleaseDate().format(Constants.RELEASE_FORMATTER));
        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new RuntimeException("Переданные параметры фильма не содержат ID");
        }

        checkFilmExists(film.getId());
        checkFilmName(film);

        filmStorage.putFilm(film);
        log.info("Данные фильма с ID: " + film.getId() + " обновлены name: " + film.getName() + ", release: "
                + film.getReleaseDate().format(Constants.RELEASE_FORMATTER));
        return film;
    }

    public ResponseDefault deleteFilm(Integer id) {
        checkFilmExists(id);

        filmStorage.deleteFilm(id);
        String message = "Фильм с ID: " + id + " успешно удален";
        log.info(message);
        return new ResponseDefault(message, HttpStatus.OK);
    }

    public ResponseDefault addLike(Integer filmId, Integer userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        if (filmStorage.getFilm(filmId).addLike(userId)) {
            String message = "Пользователь с ID: " + userId + " поставил like фильму "
                    + filmStorage.getFilm(filmId).getName() + " с ID: " + filmId;
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Пользователь с ID: " + userId + " уже поставил like фильму "
                    + filmStorage.getFilm(filmId).getName() + " с ID: " + filmId);
        }
    }

    public ResponseDefault removeLike(Integer filmId, Integer userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        if (filmStorage.getFilm(filmId).removeLike(userId)) {
            String message = "Пользователь с ID: " + userId + " удалил like фильму "
                    + filmStorage.getFilm(filmId).getName() + " с ID: " + filmId;
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Пользователь с ID: " + userId + " не ставил like фильму "
                    + filmStorage.getFilm(filmId).getName() + " с ID: " + filmId);
        }
    }

    public List<Film> getPopular(Integer count) {
        log.info("Запрос " + count + " самых популярных фильмов");
        return filmStorage.getFilms().values().stream()
                .sorted(Comparator.comparing(Film::getLikesAmount,
                        Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(Film::getId))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkFilmExists(Integer filmId) {
        if (!filmStorage.getFilms().containsKey(filmId)) {
            throw new NotFoundException("Фильм с ID: " + filmId + " не существует");
        }
    }

    private void checkFilmName(Film film) {
        if ((film.getId() == null)
                && filmStorage.getFilms().values().stream().anyMatch(f -> f.getName().equals(film.getName()))) {
            throw new AlreadyExistsException("Фильм " + film.getName() + " уже существует");
        } else {
            for (Film f : filmStorage.getFilms().values()) {
                if (!Objects.equals(film.getId(), f.getId()) && film.getName().equals(f.getName())) {
                    throw new AlreadyExistsException("Фильм " + film.getName() + " уже существует");
                }
            }
        }
    }

    private void checkUserExists(Integer userId) {
        if (!userStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не существует");
        }
    }
}
