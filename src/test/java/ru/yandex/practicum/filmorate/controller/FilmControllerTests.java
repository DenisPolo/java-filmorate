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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ResponseDefault;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
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
    Film film;
    Film film1;
    Film film2;
    Film film3;
    Film film4;
    User user;
    User user1;
    User user2;
    User user3;
    User user4;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void beforeEach() {
        url = URI.create("http://localhost:" + port + "/films");

        film = Film.builder()
                .name("FilmName")
                .description("Any film description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        film1 = Film.builder()
                .name("Film1Name")
                .description("Any film1 description")
                .releaseDate(LocalDate.of(1981, 12, 1))
                .duration(120)
                .build();

        film2 = Film.builder()
                .name("Film2Name")
                .description("Any film2 description")
                .releaseDate(LocalDate.of(1979, 6, 15))
                .duration(84)
                .build();

        film3 = Film.builder()
                .name("Film3Name")
                .description("Any film3 description")
                .releaseDate(LocalDate.of(2001, 9, 11))
                .duration(135)
                .build();

        film4 = Film.builder()
                .name("Film4Name")
                .description("Any film4 description")
                .releaseDate(LocalDate.of(2015, 4, 10))
                .duration(103)
                .build();

        user = User.builder()
                .email("mail@yandex.ru")
                .login("userLogin")
                .name("UserName")
                .birthday(LocalDate.of(2005, 1, 22))
                .build();

        user1 = User.builder()
                .email("mailUser1@yandex.ru")
                .login("user1Login")
                .name("User1Name")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        user2 = User.builder()
                .email("mailUser2@yandex.ru")
                .login("user2Login")
                .name("User2Name")
                .birthday(LocalDate.of(1984, 11, 10))
                .build();

        user3 = User.builder()
                .email("mailUser3@yandex.ru")
                .login("user3Login")
                .name("User3Name")
                .birthday(LocalDate.of(1986, 5, 12))
                .build();

        user4 = User.builder()
                .email("mailUser4@yandex.ru")
                .login("user4Login")
                .name("User4Name")
                .birthday(LocalDate.of(1998, 1, 5))
                .build();
    }

    @Test
    public void getAllFilms_shouldReturnEmptyList_whenNoAnyFilms() throws Exception {
        assertThat(this.restTemplate.getForObject(url, String.class)).contains("[]");
    }

    @Test
    public void addFilm_shouldAddNewFilmWhithId1AndReturnObjectFilm_whenCorrectObjectFilmRequest() throws Exception {
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
        film1.setId(1);
        film2.setId(2);

        assertThat(this.restTemplate.postForObject(url, film1, Film.class).equals(film1));
        assertThat(this.restTemplate.postForObject(url, film2, Film.class).equals(film2));
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url, Film[].class);
        List<Film> filmsList = Arrays.asList(getFilmsResponse.getBody());

        assertEquals(filmsList.get(0), film1);
        assertEquals(filmsList.get(1), film2);
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenFilmsNaimIsEmptyOrNull() throws Exception {
        film1.setName("");
        film2.setName(null);

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film1, ResponseError.class);
        ResponseEntity<ResponseError> postFilm2Response = restTemplate.postForEntity(url, film2, ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Имя не должно быть пустым");
        assertSame(postFilm2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm2Response.getBody().getMessage(), "Имя не должно быть пустым");
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenFilmsDescriptionMoreThan200Symbols() throws Exception {
        //description contains 201 symbol
        film.setDescription("01234567890123456789012345678901234567890123456789" +
                "01234567890123456789012345678901234567890123456789" +
                "01234567890123456789012345678901234567890123456789" +
                "01234567890123456789012345678901234567890123456789" + "0");

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film, ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Описание не должно превышать 200 символов");
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenFilmsRelease1895December28OrEarlier() throws Exception {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film, ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Не верно указана дата релиза");
    }

    @Test
    public void addFilm_shouldReturnBadRequest_whenNegativeFilmsDuration() throws Exception {
        film.setDuration(-95);

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url, film,
                ResponseError.class);

        assertSame(postFilm1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postFilm1Response.getBody().getMessage(), "Продолжительность фильма не может быть отрицательной");
    }

    @Test
    public void updateFilm_shouldUpdate_whenCorrectObjectsFilmsRequestAndExistingUpdatingId() throws Exception {
        film2.setId(1);

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
    public void updateFilm_shouldReturnNotFound_whenUpdatingIdDoesNotExist() throws Exception {
        film1.setId(1);
        film2.setId(2);

        restTemplate.postForLocation(url, film1);
        ResponseEntity<ResponseError> putFilm2Response = restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(film2), ResponseError.class);
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url, Film[].class);
        List<Film> filmsList = Arrays.asList(Objects.requireNonNull(getFilmsResponse.getBody()));

        assertSame(putFilm2Response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(putFilm2Response.getBody().getMessage(), "Фильм с ID: 2 не существует");
        assertEquals(filmsList.get(0), film1);
    }

    @Test
    public void getFilm_shouldReturnNotFound_whenFilmWithIdDoesNotExist() {
        ResponseEntity<ResponseError> getFilmById3response = restTemplate.getForEntity(url.resolve("/films/3"),
                ResponseError.class);

        assertSame(getFilmById3response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(getFilmById3response.getBody().getMessage(), "Фильм с ID: 3 не существует");
    }

    @Test
    public void getFilm_shouldReturnFilmWithId1_whenFilmWithIdExists() {
        film.setId(1);

        restTemplate.postForLocation(url, film);
        ResponseEntity<Film> getFilmById1response = restTemplate.getForEntity(url.resolve("/films/1"), Film.class);

        assertSame(getFilmById1response.getStatusCode(), HttpStatus.OK);
        assertEquals(getFilmById1response.getBody(), film);
    }

    @Test
    public void deleteFilm_shouldReturnNotFound_whenFilmWithIdDoesNotExist() {
        ResponseEntity<ResponseError> deleteFilmById3response = restTemplate.exchange(url.resolve("/films/3"),
                HttpMethod.DELETE, new HttpEntity<>(null), ResponseError.class);

        assertSame(deleteFilmById3response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(deleteFilmById3response.getBody().getMessage(), "Фильм с ID: 3 не существует");
    }

    @Test
    public void deleteFilm_shouldDeleteFilmWithId1_whenFilmWithIdExists() {
        film.setId(1);
        restTemplate.postForLocation(url, film);

        ResponseEntity<Film> getFilmById1Response1 = restTemplate.getForEntity(url.resolve("/films/1"), Film.class);
        ResponseEntity<ResponseDefault> deleteFilmById1Response = restTemplate.exchange(url.resolve("/films/1"),
                HttpMethod.DELETE, new HttpEntity<>(null), ResponseDefault.class);
        ResponseEntity<ResponseError> getFilmById1Response2 = restTemplate.getForEntity(url.resolve("/films/1"),
                ResponseError.class);

        assertSame(getFilmById1Response1.getStatusCode(), HttpStatus.OK);
        assertEquals(getFilmById1Response1.getBody(), film);
        assertSame(deleteFilmById1Response.getStatusCode(), HttpStatus.OK);
        assertEquals(deleteFilmById1Response.getBody().getMessage(), "Фильм с ID: 1 успешно удален");
        assertSame(getFilmById1Response2.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(getFilmById1Response2.getBody().getMessage(), "Фильм с ID: 1 не существует");
    }

    @Test
    public void addLike_shouldReturnOk_whenFilmAndUserExists() {
        restTemplate.postForLocation(url, film);
        restTemplate.postForLocation(url.resolve("/users"), user);

        ResponseEntity<ResponseDefault> addLike = restTemplate.exchange(url.resolve("/films/1/like/1"),
                HttpMethod.PUT, new HttpEntity<>(null), ResponseDefault.class);

        assertSame(addLike.getStatusCode(), HttpStatus.OK);
        assertEquals(addLike.getBody().getMessage(), "Пользователь с ID: 1 поставил like фильму FilmName с ID: 1");
    }

    @Test
    public void addLike_shouldReturnInternalServerError_whenLikeAlreadyAdded() {
        restTemplate.postForLocation(url, film);
        restTemplate.postForLocation(url.resolve("/users"), user);
        restTemplate.put(url.resolve("/films/1/like/1"), null);

        ResponseEntity<ResponseError> addLike = restTemplate.exchange(url.resolve("/films/1/like/1"),
                HttpMethod.PUT, new HttpEntity<>(null), ResponseError.class);

        assertSame(addLike.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(addLike.getBody().getMessage(), "Пользователь с ID: 1 уже поставил like фильму FilmName с ID: 1");
    }

    @Test
    public void removeLike_shouldReturnOk_whenLikeExists() {
        restTemplate.postForLocation(url, film);
        restTemplate.postForLocation(url.resolve("/users"), user);
        restTemplate.put(url.resolve("/films/1/like/1"), null);

        ResponseEntity<ResponseDefault> removeLike = restTemplate.exchange(url.resolve("/films/1/like/1"),
                HttpMethod.DELETE, new HttpEntity<>(null), ResponseDefault.class);

        assertSame(removeLike.getStatusCode(), HttpStatus.OK);
        assertEquals(removeLike.getBody().getMessage(), "Пользователь с ID: 1 удалил like фильму FilmName с ID: 1");
    }

    @Test
    public void removeLike_shouldReturnInternalServerError_whenLikeDoesNotExist() {
        restTemplate.postForLocation(url, film);
        restTemplate.postForLocation(url.resolve("/users"), user);

        ResponseEntity<ResponseError> removeLike = restTemplate.exchange(url.resolve("/films/1/like/1"),
                HttpMethod.DELETE, new HttpEntity<>(null), ResponseError.class);

        assertSame(removeLike.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(removeLike.getBody().getMessage(), "Пользователь с ID: 1 не ставил like фильму FilmName с ID: 1");
    }

    @Test
    public void getPopular_shouldReturnEmptyList_whenNoFilms() {
        assertThat(this.restTemplate.getForObject(url.resolve("/films/popular"), String.class)).contains("[]");
    }

    @Test
    public void getPopular_shouldReturnMostPopularFilmsList_whenFilmsExists() {
        restTemplate.postForLocation(url, film);
        restTemplate.postForLocation(url, film1);
        restTemplate.postForLocation(url, film2);
        restTemplate.postForLocation(url, film3);
        restTemplate.postForLocation(url, film4);
        restTemplate.postForLocation(url.resolve("/users"), user);
        restTemplate.postForLocation(url.resolve("/users"), user1);
        restTemplate.postForLocation(url.resolve("/users"), user2);
        restTemplate.postForLocation(url.resolve("/users"), user3);
        restTemplate.postForLocation(url.resolve("/users"), user4);
        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= i; j++) {
                restTemplate.put(url.resolve("/films/" + i + "/like/" + j), null);
            }
        }

        ResponseEntity<Film[]> getFilms = restTemplate.exchange(url.resolve("/films/"),
                HttpMethod.GET, new HttpEntity<>(null), Film[].class);
        List<Film> checkingList = Arrays.asList(getFilms.getBody());
        ResponseEntity<Film[]> getPopular = restTemplate.exchange(url.resolve("/films/popular"),
                HttpMethod.GET, new HttpEntity<>(null), Film[].class);
        List<Film> popularList = Arrays.asList(getPopular.getBody());
        Collections.reverse(checkingList);
        Collections.rotate(checkingList, -1);

        assertSame(getPopular.getStatusCode(), HttpStatus.OK);
        assertEquals(popularList, checkingList);
    }
}