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
import ru.yandex.practicum.filmorate.service.ResponseDefault;

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
        url = URI.create("http://localhost:" + port + "/users");

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
    public void createUser_shouldCreateNewUserWhithId1AndReturnObjectUser_whenCorrectObjectUserRequest() {
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
    public void createUser_shouldCreateTwoFilms_whenCorrectObjectsUsersRequest() {
        user1.setId(1);
        user2.setId(2);

        assertThat(this.restTemplate.postForObject(url, user1, User.class).equals(user1));
        assertThat(this.restTemplate.postForObject(url, user2, User.class).equals(user2));
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url, User[].class);
        List<User> usersList = Arrays.asList(getUsersResponse.getBody());

        assertEquals(usersList.get(0), user1);
        assertEquals(usersList.get(1), user2);
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserEmailIsEmptyOrNull() {
        user1.setEmail("");
        user2.setEmail(null);

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url, user1, ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url, user2, ResponseError.class);

        assertSame(postUser1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser1Response.getBody().getMessage(), "Email не должен быть пустым");
        assertSame(postUser2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser2Response.getBody().getMessage(), "Email не должен быть пустым");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserEmailDoesNotMatchMailFormat() {
        user1.setEmail("mailUser1@");
        user2.setEmail("mail User2@yandex.ru");
        user3.setEmail("@mailUser3");

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
    public void createUser_shouldReturnBadRequest_whenUserLoginIsEmptyOrNull() {
        user1.setLogin("");
        user2.setLogin(null);

        ResponseEntity<ResponseError> postUser1Response = restTemplate.postForEntity(url, user1, ResponseError.class);
        ResponseEntity<ResponseError> postUser2Response = restTemplate.postForEntity(url, user2, ResponseError.class);

        assertSame(postUser1Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser1Response.getBody().getMessage(), "Login не должен быть пустым");
        assertSame(postUser2Response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUser2Response.getBody().getMessage(), "Login не должен быть пустым");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserLoginContainsSpaceSymbol() {
        user.setLogin("user login");

        ResponseEntity<ResponseError> postUserResponse = restTemplate.postForEntity(url, user, ResponseError.class);

        assertSame(postUserResponse.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUserResponse.getBody().getMessage(), "Login не должен содержать пробелы");
    }

    @Test
    public void createUser_shouldReturnBadRequest_whenUserBirthdayIsInTheFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));

        ResponseEntity<ResponseError> postUserResponse = restTemplate.postForEntity(url, user, ResponseError.class);

        assertSame(postUserResponse.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(postUserResponse.getBody().getMessage(), "День рождения не может быть в будущем");
    }

    @Test
    public void updateUser_shouldUpdate_whenCorrectObjectsUsersRequestAndExistingUpdatingId() {
        user2.setId(1);

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
    public void updateUser_shouldReturnNotFound_whenUpdatingIdDoesNotExist() {
        user1.setId(1);
        user2.setId(2);

        restTemplate.postForLocation(url, user1);
        ResponseEntity<ResponseError> putUser2response = restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(user2), ResponseError.class);
        ResponseEntity<User[]> getUsersResponse = restTemplate.getForEntity(url, User[].class);
        List<User> usersList = Arrays.asList(getUsersResponse.getBody());

        assertSame(putUser2response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(putUser2response.getBody().getMessage(), "Пользователь с ID: 2 не существует");
        assertEquals(usersList.get(0), user1);
    }

    @Test
    public void getUserById_shouldReturnNotFound_whenUserWithIdDoesNotExist() {
        ResponseEntity<ResponseError> getUserById3response = restTemplate.getForEntity(url.resolve("/users/3"),
                ResponseError.class);

        assertSame(getUserById3response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(getUserById3response.getBody().getMessage(), "Пользователь с ID: 3 не существует");
    }

    @Test
    public void getUserById_shouldReturnUserWithId1_whenUserWithIdExists() {
        user.setId(1);

        restTemplate.postForLocation(url, user);
        ResponseEntity<User> getUserById1response = restTemplate.getForEntity(url.resolve("/users/1"), User.class);

        assertSame(getUserById1response.getStatusCode(), HttpStatus.OK);
        assertEquals(getUserById1response.getBody(), user);
    }

    @Test
    public void deleteUser_shouldReturnNotFound_whenUserWithIdDoesNotExist() {
        ResponseEntity<ResponseError> deleteUserById3response = restTemplate.exchange(url.resolve("/users/3"),
                HttpMethod.DELETE, new HttpEntity<>(null), ResponseError.class);

        assertSame(deleteUserById3response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(deleteUserById3response.getBody().getMessage(), "Пользователь с ID: 3 не существует");
    }

    @Test
    public void deleteUser_shouldDeleteUserWithId1_whenUserWithIdExists() {
        user.setId(1);
        restTemplate.postForLocation(url, user);

        ResponseEntity<User> getUserById1response1 = restTemplate.getForEntity(url.resolve("/users/1"), User.class);
        ResponseEntity<ResponseDefault> deleteUserById1response = restTemplate.exchange(url.resolve("/users/1"),
                HttpMethod.DELETE, new HttpEntity<>(null), ResponseDefault.class);
        ResponseEntity<ResponseError> getUserById1response2 = restTemplate.getForEntity(url.resolve("/users/1"),
                ResponseError.class);

        assertSame(getUserById1response1.getStatusCode(), HttpStatus.OK);
        assertEquals(getUserById1response1.getBody(), user);
        assertSame(deleteUserById1response.getStatusCode(), HttpStatus.OK);
        assertEquals(deleteUserById1response.getBody().getMessage(), "Пользователь с ID: 1 успешно удален");
        assertSame(getUserById1response2.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(getUserById1response2.getBody().getMessage(), "Пользователь с ID: 1 не существует");
    }

    @Test
    public void getUserFriends_shouldReturnEmptyList_whenNoAnyFriends() {
        restTemplate.postForLocation(url, user);

        ResponseEntity<String> getUserFriendsResponse = restTemplate.getForEntity(url.resolve("/users/1/friends"),
                String.class);

        assertSame(getUserFriendsResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(getUserFriendsResponse.getBody(), "[]");
    }

    @Test
    public void getUserFriends_shouldReturnFriendsList_whenHaveFewFriends() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);
        restTemplate.postForLocation(url, user3);
        restTemplate.put(url.resolve("/users/3/friends/1"), null);
        restTemplate.put(url.resolve("/users/3/friends/2"), null);

        ResponseEntity<User> getUserById1response = restTemplate.getForEntity(url.resolve("/users/1"), User.class);
        ResponseEntity<User> getUserById2response = restTemplate.getForEntity(url.resolve("/users/2"), User.class);
        ResponseEntity<User[]> getUserFriendsResponse = restTemplate.getForEntity(url.resolve("/users/3/friends"),
                User[].class);
        List<User> friendsList = Arrays.asList(getUserFriendsResponse.getBody());

        assertSame(getUserFriendsResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(friendsList.get(0), getUserById1response.getBody());
        assertEquals(friendsList.get(1), getUserById2response.getBody());
    }

    @Test
    public void getCommonFriends_shouldReturnEmptyList_whenNoFriendsAtAll() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);

        ResponseEntity<String> getCommonFriendsResponse =
                restTemplate.getForEntity(url.resolve("/users/1/friends/common/2"), String.class);

        assertSame(getCommonFriendsResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(getCommonFriendsResponse.getBody(), "[]");
    }

    @Test
    public void getCommonFriends_shouldReturnEmptyList_whenNoCommonFriends() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);
        restTemplate.postForLocation(url, user3);
        restTemplate.postForLocation(url, user4);
        restTemplate.put(url.resolve("/users/1/friends/2"), null);
        restTemplate.put(url.resolve("/users/1/friends/4"), null);
        restTemplate.put(url.resolve("/users/4/friends/3"), null);
        restTemplate.put(url.resolve("/users/4/friends/1"), null);

        ResponseEntity<String> getCommonFriendsResponse =
                restTemplate.getForEntity(url.resolve("/users/1/friends/common/4"), String.class);

        assertSame(getCommonFriendsResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(getCommonFriendsResponse.getBody(), "[]");
    }

    @Test
    public void getCommonFriends_shouldReturnCommonFriendsList_whenAnyCommonFriends() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);
        restTemplate.postForLocation(url, user3);
        restTemplate.postForLocation(url, user4);
        restTemplate.put(url.resolve("/users/1/friends/2"), null);
        restTemplate.put(url.resolve("/users/1/friends/3"), null);
        restTemplate.put(url.resolve("/users/4/friends/2"), null);
        restTemplate.put(url.resolve("/users/4/friends/3"), null);

        ResponseEntity<User> getUserById2response = restTemplate.getForEntity(url.resolve("/users/2"), User.class);
        ResponseEntity<User> getUserById3response = restTemplate.getForEntity(url.resolve("/users/3"), User.class);
        ResponseEntity<User[]> getCommonFriendsResponse =
                restTemplate.getForEntity(url.resolve("/users/1/friends/common/4"), User[].class);
        List<User> commonFriends = List.of(getCommonFriendsResponse.getBody());

        assertSame(getCommonFriendsResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(commonFriends.get(0), getUserById2response.getBody());
        assertEquals(commonFriends.get(1), getUserById3response.getBody());
    }

    @Test
    public void addFriend_shouldReturnOk_whenAddingFriends() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);

        ResponseEntity<ResponseDefault> putUser1Friend2Response =
                restTemplate.exchange(url.resolve("/users/1/friends/2"), HttpMethod.PUT, new HttpEntity<>(null),
                        ResponseDefault.class);

        assertSame(putUser1Friend2Response.getStatusCode(), HttpStatus.OK);
        assertEquals(putUser1Friend2Response.getBody().getMessage(),
                "Пользователи с ID: 1 и ID: 2 успешно добавлены в друзья");
    }

    @Test
    public void addFriend_shouldReturnInternalServerError_whenAlreadyFriends() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);
        restTemplate.put(url.resolve("/users/1/friends/2"), null);

        ResponseEntity<ResponseError> putUser1Friend2Response =
                restTemplate.exchange(url.resolve("/users/1/friends/2"), HttpMethod.PUT, new HttpEntity<>(null),
                        ResponseError.class);

        assertSame(putUser1Friend2Response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(putUser1Friend2Response.getBody().getMessage(), "Пользователи с ID: 1 и ID: 2 уже друзья");
    }

    @Test
    public void deleteFriend_shouldReturnInternalServerError_whenUsersAreNotFriends() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);

        ResponseEntity<ResponseError> deleteUser1Friend2Response =
                restTemplate.exchange(url.resolve("/users/1/friends/2"), HttpMethod.DELETE, new HttpEntity<>(null),
                        ResponseError.class);

        assertSame(deleteUser1Friend2Response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(deleteUser1Friend2Response.getBody().getMessage(),
                "Пользователи с ID: 1 и ID: 2 не являются друзьями");
    }

    @Test
    public void deleteFriend_shouldReturnOk_whenUsersAreFriends() {
        restTemplate.postForLocation(url, user1);
        restTemplate.postForLocation(url, user2);
        restTemplate.put(url.resolve("/users/1/friends/2"), null);

        ResponseEntity<ResponseError> deleteUser1Friend2Response =
                restTemplate.exchange(url.resolve("/users/1/friends/2"), HttpMethod.DELETE, new HttpEntity<>(null),
                        ResponseError.class);

        assertSame(deleteUser1Friend2Response.getStatusCode(), HttpStatus.OK);
        assertEquals(deleteUser1Friend2Response.getBody().getMessage(),
                "Пользователи с ID: 1 и ID: 2 больше не друзья :(");
    }
}