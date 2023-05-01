package ru.yandex.practicum.filmorate;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

public class Constants {
    public static final LocalDate START_RELEASE_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    public static final DateTimeFormatter RELEASE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
