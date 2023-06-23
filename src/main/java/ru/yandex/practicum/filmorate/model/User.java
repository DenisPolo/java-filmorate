package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {
    private Integer id;

    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email не соответстует формату адреса электронной почты")
    private String email;

    @NotBlank(message = "Login не должен быть пустым")
    @Pattern(regexp = "[^ ]*", message = "Login не должен содержать пробелы")
    private String login;

    private String name;

    @Past(message = "День рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Long> friends;

    public User(Integer id, String email, String login, String name, LocalDate birthday, Set<Long> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = friends;
    }
}