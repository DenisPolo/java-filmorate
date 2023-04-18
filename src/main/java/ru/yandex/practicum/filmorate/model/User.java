package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

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

    public boolean addFriend(Integer friendsId) {
        if (friends == null) {
            friends = new TreeSet<>();
        }
        return friends.add(friendsId.longValue());
    }

    public boolean removeFriend(Integer friendsId) {
        if (friends == null) {
            return false;
        }
        return friends.remove(friendsId.longValue());
    }
}