package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ResponseDefault;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping()
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilms(@PathVariable Integer id) {
        return filmService.getFilm(id);
    }

    @PostMapping()
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteFilm(@PathVariable Integer id) {
        return ResponseEntity.ok(filmService.deleteFilm(id));
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity addLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        return ResponseEntity.ok(filmService.addLike(filmId, userId));
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseEntity removeLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        return  ResponseEntity.ok(filmService.removeLike(filmId, userId));
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getPopular(count);
    }
}