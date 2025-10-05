package ru.otus.hw.jpa.services;

import ru.otus.hw.jpa.models.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> findAll();
}
