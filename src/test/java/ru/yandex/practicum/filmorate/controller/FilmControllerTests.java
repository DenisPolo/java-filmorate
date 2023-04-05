package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.ResponseError;
import ru.yandex.practicum.filmorate.model.Film;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmControllerTests {
    URI url;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void beforeEach() {
        url = URI.create("http://localhost:" + port + "/films");
    }

    @Test
    public void getAllFilms_shouldReturnEmptyList_whenNoAnyFilms() throws Exception {
        assertThat(this.restTemplate.getForObject(url, String.class)).contains("[]");
    }

    @Test
    public void addFilm_shouldAddNewFilmWhithId1AndReturnObjectFilm_whenCorrectObjectFilmRequest() throws Exception {
        Film film = Film.builder()
                .name("FilmName")
                .description("Any film description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        ResponseEntity<Film> postFilmResponse = restTemplate.postForEntity(url, film, Film.class);

        assertEquals(postFilmResponse.getBody().getId(), 1);
        assertEquals(postFilmResponse.getBody().getName(), film.getName());
        assertEquals(postFilmResponse.getBody().getDescription(), film.getDescription());
        assertEquals(postFilmResponse.getBody().getReleaseDate(), film.getReleaseDate());
        assertEquals(postFilmResponse.getBody().getDuration(), film.getDuration());
        assertThat(this.restTemplate.getForObject(url, String.class))
                .contains("\"id\":1").contains(film.getName()).contains(film.getDescription())
                .contains(film.getReleaseDate().toString()).contains(film.getDuration().toString());
    }

    @Test
    public void addFilm_shouldAddTwoFilms_whenCorrectObjectsFilmsRequest() throws Exception {
        Film film1 = Film.builder()
                .id(1)
                .name("Film1Name")
                .description("Any film1 description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        Film film2 = Film.builder()
                .id(2)
                .name("Film2Name")
                .description("Any film2 description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        assertThat(this.restTemplate.postForObject(url, film1, Film.class).equals(film1));
        assertThat(this.restTemplate.postForObject(url, film2, Film.class).equals(film2));
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url, Film[].class);
        List<Film> filmsList = Arrays.asList(getFilmsResponse.getBody());

        assertEquals(filmsList.get(0), film1);
        assertEquals(filmsList.get(1), film2);
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenFilmsNaimIsEmptyOrNull() throws Exception {
        Film film1 = Film.builder()
                .name("")
                .description("Any film1 description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        Film film2 = Film.builder()
                .name(null)
                .description("Any film2 description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film1, ResponseError.class);
        ResponseEntity<ResponseError> postFilm2Response = restTemplate.postForEntity(url, film2, ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Имя не должно быть пустым");
        assertSame(postFilm2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm2Response.getBody().getMessage(), "Имя не должно быть пустым");
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenFilmsDescriptionMoreThan200Symbols() throws Exception {
        Film film = Film.builder()
                .name("FilmName")
                //description contains 201 symbol
                .description("01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                        "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                        "123456789012345678901234567890")
                .releaseDate(LocalDate.of(1895, 12, 29))
                .duration(95)
                .build();

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film, ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Описание не должно превышать 200 символов");
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenFilmsRelease1895December28OrEarlier() throws Exception {
        Film film = Film.builder()
                .name("FilmName")
                .description("Any film description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(95)
                .build();

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film, ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Не верно указана дата релиза");
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenNegativeFilmsDuration() throws Exception {
        Film film = Film.builder()
                .name("FilmName")
                .description("Any film description")
                .releaseDate(LocalDate.of(1895, 12, 29))
                .duration(-95)
                .build();

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film,
                ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Продолжительность фильма не может быть отрицательной");
    }

    @Test
    public void updateFilm_shouldUpdate_whenCorrectObjectsFilmsRequestAndExistingUpdatingId() throws Exception {
        Film film1 = Film.builder()
                .name("Film1Name")
                .description("Any film1 description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        Film film2 = Film.builder()
                .id(1)
                .name("Film2Name")
                .description("Any film2 description")
                .releaseDate(LocalDate.of(2000, 11, 25))
                .duration(120)
                .build();

        restTemplate.postForLocation(url, film1);
        ResponseEntity<Film> putFilm2Response = restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(film2), Film.class);
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url, Film[].class);
        List<Film> filmsList = Arrays.asList(getFilmsResponse.getBody());

        assertSame(putFilm2Response.getStatusCode(), HttpStatus.OK);
        assertEquals(putFilm2Response.getBody().getId(), 1);
        assertEquals(putFilm2Response.getBody().getName(), film2.getName());
        assertEquals(putFilm2Response.getBody().getDescription(), film2.getDescription());
        assertEquals(putFilm2Response.getBody().getReleaseDate(), film2.getReleaseDate());
        assertEquals(putFilm2Response.getBody().getDuration(), film2.getDuration());
        assertEquals(filmsList.get(0), film2);
    }

    @Test
    public void updateFilm_shouldReturnInternalServerError_whenUpdatingIdDoesNotExist() throws Exception {
        Film film1 = Film.builder()
                .id(1)
                .name("Film1Name")
                .description("Any film1 description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        Film film2 = Film.builder()
                .id(2)
                .name("Film2Name")
                .description("Any film2 description")
                .releaseDate(LocalDate.of(2000, 11, 25))
                .duration(120)
                .build();

        restTemplate.postForLocation(url, film1);
        ResponseEntity<ResponseError> putFilm2Response = restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(film2), ResponseError.class);
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url, Film[].class);
        List<Film> filmsList = Arrays.asList(Objects.requireNonNull(getFilmsResponse.getBody()));

        assertSame(putFilm2Response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(putFilm2Response.getBody().getMessage(), "Фильм с ID: 2 не существует");
        assertEquals(filmsList.get(0), film1);
    }
}