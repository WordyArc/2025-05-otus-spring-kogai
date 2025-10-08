package ru.otus.hw.persistence.rdbms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.persistence.rdbms.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> { }
