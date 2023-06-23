package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.ResponseDefault;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    public final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, Film> getFilms() {
        String sql = "SELECT  f.film_id,\n" +
                "       f.film_name,\n" +
                "       f.description,\n" +
                "       f.release,\n" +
                "       f.duration,\n" +
                "       mpa.mpa_id,\n" +
                "       mpa.mpa_name,\n" +
                "       fgj.genre_ids,\n" +
                "       fgj.genre_names,\n" +
                "       l.likes\n" +
                "FROM films AS f\n" +
                "LEFT OUTER JOIN mpa AS mpa ON f.mpa_id = mpa.mpa_id\n" +
                "LEFT OUTER JOIN (SELECT fg.film_id, \n" +
                "               STRING_AGG (fg.genre_id, ', ') AS genre_ids,\n" +
                "               STRING_AGG (g.genre_name, ', ') AS genre_names\n" +
                "               FROM film_genres AS fg\n" +
                "               LEFT OUTER JOIN genres AS g ON fg.genre_id = g.genre_id\n" +
                "               GROUP BY fg.film_id) AS fgj ON f.film_id = fgj.film_id\n" +
                "LEFT OUTER JOIN (SELECT film_id,\n" +
                "               STRING_AGG (user_id, ', ') AS likes\n" +
                "               FROM likes\n" +
                "               GROUP BY film_id) AS l ON f.film_id = l.film_id\n" +
                "ORDER BY f.film_id ASC;";

        List<Film> filmsList = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));

        return filmsList.stream().collect(Collectors.toMap(Film::getId, film -> film));
    }

    @Override
    public Film getFilm(Integer id) {
        checkFilmExists(id);

        String sql = "SELECT  f.film_id,\n" +
                "       f.film_name,\n" +
                "       f.description,\n" +
                "       f.release,\n" +
                "       f.duration,\n" +
                "       mpa.mpa_id,\n" +
                "       mpa.mpa_name,\n" +
                "       fgj.genre_ids,\n" +
                "       fgj.genre_names,\n" +
                "       l.likes\n" +
                "FROM films AS f\n" +
                "LEFT OUTER JOIN mpa AS mpa ON f.mpa_id = mpa.mpa_id\n" +
                "LEFT OUTER JOIN (SELECT fg.film_id, \n" +
                "               STRING_AGG (fg.genre_id, ', ') AS genre_ids,\n" +
                "               STRING_AGG (g.genre_name, ', ') AS genre_names\n" +
                "               FROM film_genres AS fg\n" +
                "               LEFT OUTER JOIN genres AS g ON fg.genre_id = g.genre_id\n" +
                "               GROUP BY fg.film_id) AS fgj ON f.film_id = fgj.film_id\n" +
                "LEFT OUTER JOIN (SELECT film_id,\n" +
                "               STRING_AGG (user_id, ', ') AS likes\n" +
                "               FROM likes\n" +
                "               GROUP BY film_id) AS l ON f.film_id = l.film_id\n" +
                "WHERE f.film_id = ?;";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    @Override
    public Integer putFilm(Film film) {
        checkFilmName(film.getId(), film.getName());

        if (film.getId() == null) {
            String sqlQuery = "INSERT INTO films(film_name, description, release, duration, mpa_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
                stmt.setString(1, film.getName());
                stmt.setString(2, film.getDescription());
                stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
                stmt.setInt(4, film.getDuration());
                stmt.setInt(5, (film.getMpa() == null) ? 0 : film.getMpa().getId());
                return stmt;
            }, keyHolder);

            Integer id = keyHolder.getKey().intValue();

            film.setId(id);
            updateGenre(film);

            return id;
        } else {
            checkFilmExists(film.getId());

            String sqlQuery = "UPDATE films SET film_name = ?, description = ?, release = ?, duration = ?, " +
                    "mpa_id = ? WHERE film_id = ?";

            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    (film.getMpa() == null) ? 1 : film.getMpa().getId(),
                    film.getId());

            updateGenre(film);

            return film.getId();
        }
    }

    @Override
    public boolean deleteFilm(Integer id) {
        checkFilmExists(id);
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public ResponseDefault addLike(Integer filmId, Integer userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        if (jdbcTemplate.queryForRowSet("SELECT film_id FROM likes WHERE film_id = ? AND user_id = ?",
                filmId, userId).next()) {
            throw new AlreadyExistsException("Пользователь с ID: " + userId + " уже поставил like фильму "
                    + getFilm(filmId).getName() + " с ID: " + filmId);
        } else if (jdbcTemplate.update("INSERT INTO likes (film_id, user_id, last_update) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP)", filmId, userId) > 0) {
            String message = "Пользователь с ID: " + userId + " поставил like фильму "
                    + getFilm(filmId).getName() + " с ID: " + filmId;
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Ошибка в обработке добавления лайка");
        }
    }

    @Override
    public ResponseDefault removeLike(Integer filmId, Integer userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);

        if (!jdbcTemplate.queryForRowSet("SELECT film_id, user_id FROM likes WHERE film_id = ? AND user_id = ?",
                filmId, userId).next()) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не ставил like фильму "
                    + getFilm(filmId).getName() + " с ID: " + filmId);
        } else if (jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId) > 0) {
            String message = "Пользователь с ID: " + userId + " удалил like фильму "
                    + getFilm(filmId).getName() + " с ID: " + filmId;
            log.info(message);
            return new ResponseDefault(message, HttpStatus.OK);
        } else {
            throw new AlreadyExistsException("Ошибка в обработке удаления лайка");
        }
    }

    public List<Film> getPopular(Integer count) {
        String sql = "SELECT  f.film_id,\n" +
                "       f.film_name,\n" +
                "       f.description,\n" +
                "       f.release,\n" +
                "       f.duration,\n" +
                "       mpa.mpa_id,\n" +
                "       mpa.mpa_name,\n" +
                "       fgj.genre_ids,\n" +
                "       fgj.genre_names,\n" +
                "       l.likes,\n" +
                "       l.likes_count\n" +
                "FROM films AS f\n" +
                "LEFT OUTER JOIN mpa AS mpa ON f.mpa_id = mpa.mpa_id\n" +
                "LEFT OUTER JOIN (SELECT fg.film_id, \n" +
                "               STRING_AGG (fg.genre_id, ', ') AS genre_ids,\n" +
                "               STRING_AGG (g.genre_name, ', ') AS genre_names\n" +
                "               FROM film_genres AS fg\n" +
                "               LEFT OUTER JOIN genres AS g ON fg.genre_id = g.genre_id\n" +
                "               GROUP BY fg.film_id) AS fgj ON f.film_id = fgj.film_id\n" +
                "LEFT OUTER JOIN (SELECT film_id,\n" +
                "               STRING_AGG (user_id, ', ') AS likes,\n" +
                "               COUNT (user_id) AS likes_count\n" +
                "               FROM likes\n" +
                "               GROUP BY film_id) AS l ON f.film_id = l.film_id\n" +
                "GROUP BY f.film_id\n" +
                "ORDER BY l.likes_count DESC, f.film_id ASC\n" +
                "LIMIT ?;\n";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        System.out.println(rs);
        Integer id = rs.getInt("film_id");
        String name = rs.getString("film_name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release").toLocalDate();
        Integer duration = rs.getInt("duration");
        Mpa mpa = new Mpa(Integer.parseInt(rs.getString("mpa_id")), rs.getString("mpa_name"));
        List<Genre> genres = getGenres(rs.getString("genre_ids"), rs.getString("genre_names"));
        Set<Long> likes = ((rs.getString("likes") == null)) ? new TreeSet<>() :
                Stream.of(rs.getString("likes").split(", ")).map(Long::parseLong).collect(Collectors.toSet());

        return new Film(id, name, description, releaseDate, duration, mpa, genres, likes);
    }

    private List<Genre> getGenres(String genreIds, String genreNames) {
        List<Genre> genres = new ArrayList<>();
        if (genreIds == null || genreNames == null) {
            return genres;
        }

        String[] ids = genreIds.split(", ");
        String[] names = genreNames.split(", ");

        if (ids.length != names.length) {
            throw new RuntimeException("Ошибка формирования списка жанров");
        }

        for (int i = 0; i < ids.length; i++) {
            genres.add(new Genre(Integer.parseInt(ids[i]), names[i]));
        }

        return genres;
    }

    private void checkFilmExists(Integer id) {
        if (!jdbcTemplate.queryForRowSet("SELECT film_id FROM films WHERE film_id = ?", id).next()) {
            throw new NotFoundException("Фильм с ID: " + id + " не существует");
        }
    }

    private void checkFilmName(Integer id, String name) {
        if ((id == null && jdbcTemplate.queryForRowSet("SELECT film_name FROM films WHERE film_name = ?", name).next())
                || jdbcTemplate.queryForRowSet("SELECT film_name FROM films WHERE film_name = ? AND NOT film_id = ?",
                name, id).next()) {
            throw new NotFoundException("Фильм " + name + " уже существует");
        }
    }

    private void updateGenre(Film film) {
        String sqlQueryDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?";

        jdbcTemplate.update(sqlQueryDeleteGenres, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {

            String sqlQueryUpdateGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            for (Genre genre : film.getGenres()) {
                if (!jdbcTemplate.queryForRowSet("SELECT film_id FROM film_genres WHERE film_id = ? AND genre_id = ?",
                        film.getId(), genre.getId()).next()) {
                    jdbcTemplate.update(sqlQueryUpdateGenres,
                            film.getId(),
                            genre.getId());
                }
            }
        }
    }

    private void checkUserExists(Integer id) {
        if (!jdbcTemplate.queryForRowSet("SELECT user_id FROM users WHERE user_id = ?", id).next()) {
            throw new NotFoundException("Пользователь с ID: " + id + " не существует");
        }
    }
}