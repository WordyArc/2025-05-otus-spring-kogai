package ru.otus.hw.jpa.services;

import ru.otus.hw.jpa.models.Author;

import java.util.List;

public interface AuthorService {
    List<Author> findAll();
}
