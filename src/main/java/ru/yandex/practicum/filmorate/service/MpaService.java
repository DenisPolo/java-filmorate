package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Service
public class MpaService {
    private final MpaDao mpaDao;

    @Autowired
    public MpaService(MpaDao mpaDao) {
        this.mpaDao = mpaDao;
    }

    public List<Mpa> getAllMpa() {
        log.info("Запрос списка всех рейтингов MPA");
        return mpaDao.getAllMpa();
    }

    public Mpa getMpa(Integer id) {
        log.info("Запрос рейтинга MPA с ID: " + id);
        return mpaDao.getMpa(id);
    }
}