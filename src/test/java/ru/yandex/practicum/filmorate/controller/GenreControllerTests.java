package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.ResponseError;
import ru.yandex.practicum.filmorate.model.Genre;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GenreControllerTests {
    private URI url;
    private Genre genre1;
    private Genre genre2;
    private Genre genre3;
    private Genre genre4;
    private Genre genre5;
    private Genre genre6;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void beforeEach() {
        url = URI.create("http://localhost:" + port + "/genres");

        genre1 = Genre.builder()
                .id(1)
                .name("Комедия")
                .build();

        genre2 = Genre.builder()
                .id(2)
                .name("Драма")
                .build();

        genre3 = Genre.builder()
                .id(3)
                .name("Мультфильм")
                .build();

        genre4 = Genre.builder()
                .id(4)
                .name("Триллер")
                .build();

        genre5 = Genre.builder()
                .id(5)
                .name("Документальный")
                .build();

        genre6 = Genre.builder()
                .id(6)
                .name("Боевик")
                .build();
    }

    @Test
    public void getAllGenres_shouldReturnGenresList_WhenGetAllQuery() {
        Set<Genre> checkGenreSet = new HashSet<>();

        ResponseEntity<Genre[]> getGenreResponse = restTemplate.getForEntity(url, Genre[].class);
        Set<Genre> genreSet = new HashSet<>(List.of(getGenreResponse.getBody()));
        checkGenreSet.add(genre1);
        checkGenreSet.add(genre2);
        checkGenreSet.add(genre3);
        checkGenreSet.add(genre4);
        checkGenreSet.add(genre5);
        checkGenreSet.add(genre6);

        assertEquals(checkGenreSet, genreSet);
    }

    @Test
    public void getGenre_shouldReturnGenreWithIndicatedId() {
        ResponseEntity<Genre> getGenreById1response = restTemplate.getForEntity(url.resolve("/genres/1"), Genre.class);
        ResponseEntity<Genre> getGenreById2response = restTemplate.getForEntity(url.resolve("/genres/3"), Genre.class);
        ResponseEntity<Genre> getGenreById5response = restTemplate.getForEntity(url.resolve("/genres/6"), Genre.class);

        assertSame(getGenreById1response.getStatusCode(), HttpStatus.OK);
        assertEquals(getGenreById1response.getBody(), genre1);
        assertSame(getGenreById2response.getStatusCode(), HttpStatus.OK);
        assertEquals(getGenreById2response.getBody(), genre3);
        assertSame(getGenreById5response.getStatusCode(), HttpStatus.OK);
        assertEquals(getGenreById5response.getBody(), genre6);
    }

    @Test
    public void getGenre_shouldReturnNotFound_whenGenreWithDoesNotExistsId() {
        ResponseEntity<ResponseError> getGenreById9response = restTemplate.getForEntity(url.resolve("/genres/9"),
                ResponseError.class);

        assertSame(getGenreById9response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(getGenreById9response.getBody().getMessage(), "Жанр с ID: 9 не существует");
    }
}