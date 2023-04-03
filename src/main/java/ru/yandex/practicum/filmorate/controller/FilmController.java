package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
public class FilmController {
    private int id;
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        log.info("Запрос списка всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping(value = "/films")
    public Film addFilm(@Valid @RequestBody Film film) {
        id++;
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен фильм id: " + id + ", name: " + film.getName() + ", release: "
                + film.getReleaseDate().format(Film.RELEASE_FORMATTER));
        return film;
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        try {
            if (films.containsKey(film.getId())) {
                films.put(film.getId(), film);
                log.info("Данные фильма с ID: " + film.getId() + " обновлены name: " + film.getName() + ", release: "
                        + film.getReleaseDate().format(Film.RELEASE_FORMATTER));
            } else {
                throw new DoesNotExistException("Фильм с ID: " + film.getId() + " не существует");
            }
            return film;
        } catch (DoesNotExistException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
