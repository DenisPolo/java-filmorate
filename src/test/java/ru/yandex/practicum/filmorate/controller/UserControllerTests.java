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
import ru.yandex.practicum.filmorate.model.User;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {
    URI url;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void beforeEach() {
        url = URI.create("http://localhost:" + port + "/users");
    }

    @Test
    public void getAllUsers_shouldReturnEmptyList_whenNoAnyUsers() throws Exception {
        assertThat(this.restTemplate.getForObject(url, String.class)).contains("[]");
    }

    @Test
    public void createUser_shouldCreateNewUserWhithId1AndReturnObjectUser_whenCorrectObjectUserRequest() throws Exception {
        User user = User.builder()
                .email("mail@yandex.ru")
                .login("userLogin")
                .name("UserName")
                .birthday(LocalDate.of(1990, 10, 12))
                .build();

        ResponseEntity<User> postUserResponse = restTemplate.postForEntity(url, user, User.class);

        assertEquals(postUserResponse.getBody().getId(), 1);
        assertEquals(postUserResponse.getBody().getEmail(), user.getEmail());
        assertEquals(postUserResponse.getBody().getLogin(), user.getLogin());
        assertEquals(postUserResponse.getBody().getName(), user.getName());
        assertEquals(postUserResponse.getBody().getBirthday(), user.getBirthday());
        assertThat(this.restTemplate.getForObject(url, String.class)).contains("\"id\":1").contains(user.getEmail())
                .contains(user.getLogin()).contains(user.getName()).contains(user.getBirthday().toString());
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

        assertThat(this.restTemplate.postForObject(url, user1, User.class).equals(user1));
        assertThat(this.restTemplate.postForObject(url, user2, User.class).equals(user2));
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url, User[].class);
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

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url, user1, ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url, user2, ResponseError.class);

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

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url, user1, ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url, user2, ResponseError.class);
        ResponseEntity<ResponseError> postUser3Response = restTemplate.postForEntity(url, user3, ResponseError.class);

        assertSame(postUser1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser1Response.getBody().getMessage(),
                "Email не соответстует формату адреса электронной почты");
        assertSame(postUser2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser2Response.getBody().getMessage(),
                "Email не соответстует формату адреса электронной почты");
        assertSame(postUser3Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser3Response.getBody().getMessage(),
                "Email не соответстует формату адреса электронной почты");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserLoginIsEmptyOrNull()
            throws Exception {
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

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url, user1, ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url, user2, ResponseError.class);

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

        ResponseEntity<ResponseError> postUserResponse = restTemplate.postForEntity(url, user, ResponseError.class);

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

        ResponseEntity<ResponseError> postUserResponse = restTemplate.postForEntity(url, user, ResponseError.class);

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

        restTemplate.postForLocation(url, user1);
        ResponseEntity<User> putUser2response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(user2),
                User.class);
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url, User[].class);
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

        restTemplate.postForLocation(url, user1);
        ResponseEntity<ResponseError> putUser2response = restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(user2), ResponseError.class);
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url, User[].class);
        List<User> usersList = Arrays.asList(getUsersResponse.getBody());

        assertSame(putUser2response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(putUser2response.getBody().getMessage(), "Пользователя с ID: 2 не существует");
        assertEquals(usersList.get(0), user1);
    }
}