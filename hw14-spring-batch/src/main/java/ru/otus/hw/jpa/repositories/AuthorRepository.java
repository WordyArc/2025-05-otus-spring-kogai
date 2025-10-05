package ru.otus.hw.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.jpa.models.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> { }
