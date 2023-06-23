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
import ru.yandex.practicum.filmorate.model.Mpa;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MpaControllerTests {
    URI url;
    Mpa mpa1;
    Mpa mpa2;
    Mpa mpa3;
    Mpa mpa4;
    Mpa mpa5;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void beforeEach() {
        url = URI.create("http://localhost:" + port + "/mpa");

        mpa1 = Mpa.builder()
                .id(1)
                .name("G")
                .build();

        mpa2 = Mpa.builder()
                .id(2)
                .name("PG")
                .build();

        mpa3 = Mpa.builder()
                .id(3)
                .name("PG-13")
                .build();

        mpa4 = Mpa.builder()
                .id(4)
                .name("R")
                .build();

        mpa5 = Mpa.builder()
                .id(5)
                .name("NC-17")
                .build();
    }

    @Test
    public void getAllMpa_shouldReturnMpaList_WhenGetAllQuery() {
        List<Mpa> checkMpaList = new ArrayList<>();

        ResponseEntity<Mpa[]> getMpaResponse = restTemplate.getForEntity(url, Mpa[].class);
        List<Mpa> mpaList = Arrays.asList(getMpaResponse.getBody());
        checkMpaList.add(mpa1);
        checkMpaList.add(mpa2);
        checkMpaList.add(mpa3);
        checkMpaList.add(mpa4);
        checkMpaList.add(mpa5);

        assertEquals(checkMpaList, mpaList);
    }

    @Test
    public void getMpa_shouldReturnMpaWithIndicatedId() {
        ResponseEntity<Mpa> getMpaById1response = restTemplate.getForEntity(url.resolve("/mpa/1"), Mpa.class);
        ResponseEntity<Mpa> getMpaById2response = restTemplate.getForEntity(url.resolve("/mpa/2"), Mpa.class);
        ResponseEntity<Mpa> getMpaById5response = restTemplate.getForEntity(url.resolve("/mpa/5"), Mpa.class);

        assertSame(getMpaById1response.getStatusCode(), HttpStatus.OK);
        assertEquals(getMpaById1response.getBody(), mpa1);
        assertSame(getMpaById2response.getStatusCode(), HttpStatus.OK);
        assertEquals(getMpaById2response.getBody(), mpa2);
        assertSame(getMpaById5response.getStatusCode(), HttpStatus.OK);
        assertEquals(getMpaById5response.getBody(), mpa5);
    }

    @Test
    public void getMpa_shouldReturnNotFound_whenMpaWithDoesNotExistsId() {
        ResponseEntity<ResponseError> getMpaById9response = restTemplate.getForEntity(url.resolve("/mpa/9"),
                ResponseError.class);

        assertSame(getMpaById9response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(getMpaById9response.getBody().getMessage(), "MPA с ID: 9 не существует");
    }
}