package ru.yandex.practicum.filmorate;

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
import ru.yandex.practicum.filmorate.controller.ResponseError;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

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
public class FilmorateApplicationTests {
    URI url;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void beforeEach() {
        url = URI.create("http://localhost:" + port);
    }

    @Test
    public void getAllFilms_shouldReturnEmptyList_whenNoAnyFilms() throws Exception {
        assertThat(this.restTemplate.getForObject(url.resolve("/films"),
                String.class)).contains("[]");
    }

    @Test
    public void addFilm_shouldAddNewFilmWhithId1AndReturnObjectFilm_whenCorrectObjectFilmRequest() throws Exception {
        Film film = Film.builder()
                .name("FilmName")
                .description("Any film description")
                .releaseDate(LocalDate.of(1990, 10, 12))
                .duration(95)
                .build();

        ResponseEntity<Film> postFilmResponse = restTemplate.postForEntity(url.resolve("/films"), film, Film.class);

        assertEquals(postFilmResponse.getBody().getId(), 1);
        assertEquals(postFilmResponse.getBody().getName(), film.getName());
        assertEquals(postFilmResponse.getBody().getDescription(), film.getDescription());
        assertEquals(postFilmResponse.getBody().getReleaseDate(), film.getReleaseDate());
        assertEquals(postFilmResponse.getBody().getDuration(), film.getDuration());
        assertThat(this.restTemplate.getForObject(url.resolve("/films"),
                String.class)).contains("\"id\":1").contains(film.getName()).contains(film.getDescription())
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

        assertThat(this.restTemplate.postForObject(url.resolve("/films"), film1, Film.class).equals(film1));
        assertThat(this.restTemplate.postForObject(url.resolve("/films"), film2, Film.class).equals(film2));
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url.resolve("/films"), Film[].class);
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

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url.resolve("/films"), film1,
                ResponseError.class);
        ResponseEntity<ResponseError> postFilm2Response = restTemplate.postForEntity(url.resolve("/films"), film2,
                ResponseError.class);

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

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url.resolve("/films"), film,
                ResponseError.class);

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

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url.resolve("/films"), film,
                ResponseError.class);

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

        ResponseEntity<ResponseError> postFilm1Response = restTemplate.postForEntity(url.resolve("/films"), film,
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

        restTemplate.postForLocation(url.resolve("/films"), film1);
        ResponseEntity<Film> putFilm2Response = restTemplate.exchange(url.resolve("/films"), HttpMethod.PUT,
                new HttpEntity<>(film2), Film.class);
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url.resolve("/films"), Film[].class);
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

        restTemplate.postForLocation(url.resolve("/films"), film1);
        ResponseEntity<ResponseError> putFilm2Response = restTemplate.exchange(url.resolve("/films"), HttpMethod.PUT,
                new HttpEntity<>(film2), ResponseError.class);
        ResponseEntity<Film[]> getFilmsResponse = restTemplate.getForEntity(url.resolve("/films"), Film[].class);
        List<Film> filmsList = Arrays.asList(Objects.requireNonNull(getFilmsResponse.getBody()));

        assertSame(putFilm2Response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(putFilm2Response.getBody().getMessage(), "Фильм с ID: 2 не существует");
        assertEquals(filmsList.get(0), film1);
    }


    @Test
    public void getAllUsers_shouldReturnEmptyList_whenNoAnyUsers() throws Exception {
        assertThat(this.restTemplate.getForObject(url.resolve("/users"),
                String.class)).contains("[]");
    }

    @Test
    public void createUser_shouldCreateNewUserWhithId1AndReturnObjectUser_whenCorrectObjectUserRequest() throws Exception {
        User user = User.builder()
                .email("mail@yandex.ru")
                .login("userLogin")
                .name("UserName")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        ResponseEntity<User> postUserResponse = restTemplate.postForEntity(url.resolve("/users"), user, User.class);

        assertEquals(postUserResponse.getBody().getId(), 1);
        assertEquals(postUserResponse.getBody().getEmail(), user.getEmail());
        assertEquals(postUserResponse.getBody().getLogin(), user.getLogin());
        assertEquals(postUserResponse.getBody().getName(), user.getName());
        assertEquals(postUserResponse.getBody().getBirthday(), user.getBirthday());
        assertThat(this.restTemplate.getForObject(url.resolve("/users"),
                String.class)).contains("\"id\":1").contains(user.getEmail()).contains(user.getLogin())
                .contains(user.getName()).contains(user.getBirthday().toString());
    }

    @Test
    public void createUser_shouldCreateTwoFilms_whenCorrectObjectsUsersRequest() throws Exception {
        User user1 = User.builder()
                .id(1)
                .email("mailUser1@yandex.ru")
                .login("user1Login")
                .name("User1Name")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        User user2 = User.builder()
                .id(2)
                .email("mailUser2@yandex.ru")
                .login("user2Login")
                .name("User2Name")
                .birthday(LocalDate.of(1984, 11, 10))
                .build();

        assertThat(this.restTemplate.postForObject(url.resolve("/users"), user1, User.class).equals(user1));
        assertThat(this.restTemplate.postForObject(url.resolve("/users"), user2, User.class).equals(user2));
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url.resolve("/users"), User[].class);
        List<User> usersList = Arrays.asList(getUsersResponse.getBody());

        assertEquals(usersList.get(0), user1);
        assertEquals(usersList.get(1), user2);
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserEmailIsEmptyOrNull() throws Exception {
        User user1 = User.builder()
                .email("")
                .login("user1Login")
                .name("User1Name")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        User user2 = User.builder()
                .email(null)
                .login("user2Login")
                .name("User2Name")
                .birthday(LocalDate.of(1984, 11, 10))
                .build();

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url.resolve("/users"), user1,
                ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url.resolve("/users"), user2,
                ResponseError.class);

        assertSame(postUser1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser1Response.getBody().getMessage(), "Email не должен быть пустым");
        assertSame(postUser2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser2Response.getBody().getMessage(), "Email не должен быть пустым");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserEmailDoesNotMatchMailFormat() throws Exception {
        User user1 = User.builder()
                .email("mailUser1@")
                .login("user1Login")
                .name("User1Name")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        User user2 = User.builder()
                .email("mail User2@yandex.ru")
                .login("user2Login")
                .name("User2Name")
                .birthday(LocalDate.of(1995, 11, 15))
                .build();

        User user3 = User.builder()
                .email("@mailUser3")
                .login("user3Login")
                .name("User3Name")
                .birthday(LocalDate.of(2000, 12, 21))
                .build();

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url.resolve("/users"), user1,
                ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url.resolve("/users"), user2,
                ResponseError.class);
        ResponseEntity<ResponseError> postUser3Response = restTemplate.postForEntity(url.resolve("/users"), user3,
                ResponseError.class);

        assertSame(postUser1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser1Response.getBody().getMessage(), "Email не соответстует формату адреса электронной почты");
        assertSame(postUser2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser2Response.getBody().getMessage(), "Email не соответстует формату адреса электронной почты");
        assertSame(postUser3Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser3Response.getBody().getMessage(), "Email не соответстует формату адреса электронной почты");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserLoginIsEmptyOrNull() throws Exception {
        User user1 = User.builder()
                .email("mailUser1@yandex.ru")
                .login("")
                .name("User1Name")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        User user2 = User.builder()
                .email("mailUser2@yandex.ru")
                .login(null)
                .name("User2Name")
                .birthday(LocalDate.of(1984, 11, 10))
                .build();

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url.resolve("/users"), user1,
                ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url.resolve("/users"), user2,
                ResponseError.class);

        assertSame(postUser1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser1Response.getBody().getMessage(), "Login не должен быть пустым");
        assertSame(postUser2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser2Response.getBody().getMessage(), "Login не должен быть пустым");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserLoginContainsSpaceSymbol() throws Exception {
        User user = User.builder()
                .email("mailUser@yandex.ru")
                .login("user login")
                .name("UserName")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        ResponseEntity<ResponseError> postUserResponse = restTemplate.postForEntity(url.resolve("/users"), user,
                ResponseError.class);

        assertSame(postUserResponse.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUserResponse.getBody().getMessage(), "Login не должен содержать пробелы");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserBirthdayIsInTheFuture() throws Exception {
        User user = User.builder()
                .email("mailUser@yandex.ru")
                .login("userLogin")
                .name("UserName")
                //.birthday(LocalDate.of(1990, 10, 12))
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ResponseEntity<ResponseError> postUserResponse = restTemplate.postForEntity(url.resolve("/users"), user,
                ResponseError.class);

        assertSame(postUserResponse.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUserResponse.getBody().getMessage(), "День рождения не может быть в будущем");
    }

    @Test
    public void updateUser_shouldUpdate_whenCorrectObjectsUsersRequestAndExistingUpdatingId() throws Exception {
        User user1 = User.builder()
                .email("mailUser1@yandex.ru")
                .login("user1Login")
                .name("User1Name")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        User user2 = User.builder()
                .id(1)
                .email("mailUser2@yandex.ru")
                .login("user2Login")
                .name("User2Name")
                .birthday(LocalDate.of(1984, 11, 10))
                .build();

        restTemplate.postForLocation(url.resolve("/users"), user1);
        ResponseEntity<User> putUser2response = restTemplate.exchange(url.resolve("/users"), HttpMethod.PUT,
                new HttpEntity<>(user2), User.class);
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url.resolve("/users"), User[].class);
        List<User> usersList = Arrays.asList(getUsersResponse.getBody());

        assertSame(putUser2response.getStatusCode(), HttpStatus.OK);
        assertEquals(putUser2response.getBody().getId(), 1);
        assertEquals(putUser2response.getBody().getEmail(), user2.getEmail());
        assertEquals(putUser2response.getBody().getLogin(), user2.getLogin());
        assertEquals(putUser2response.getBody().getName(), user2.getName());
        assertEquals(putUser2response.getBody().getBirthday(), user2.getBirthday());
        assertEquals(usersList.get(0), user2);
    }

    @Test
    public void updateUser_shouldReturnInternalServerError_whenUpdatingIdDoesNotExist() throws Exception {
        User user1 = User.builder()
                .id(1)
                .email("mailUser1@yandex.ru")
                .login("user1Login")
                .name("User1Name")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        User user2 = User.builder()
                .id(2)
                .email("mailUser2@yandex.ru")
                .login("user2Login")
                .name("User2Name")
                .birthday(LocalDate.of(1984, 11, 10))
                .build();

        restTemplate.postForLocation(url.resolve("/users"), user1);
        ResponseEntity<ResponseError> putUser2response = restTemplate.exchange(url.resolve("/users"), HttpMethod.PUT,
                new HttpEntity<>(user2), ResponseError.class);
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url.resolve("/users"), User[].class);
        List<User> usersList = Arrays.asList(getUsersResponse.getBody());

        assertSame(putUser2response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(putUser2response.getBody().getMessage(), "Пользователя с ID: 2 не существует");
        assertEquals(usersList.get(0), user1);
    }
}