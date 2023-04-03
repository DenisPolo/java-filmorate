package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Builder
public class User {
    private int id;

    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email не соответстует формату адреса электронной почты")
    private String email;

    @NotBlank(message = "Login не должен быть пустым")
    @Pattern(regexp = "[^ ]*", message = "Login не должен содержать пробелы")
    private String login;

    private String name;

    @Past(message = "День рождения не может быть в будущем")
    private LocalDate birthday;
}