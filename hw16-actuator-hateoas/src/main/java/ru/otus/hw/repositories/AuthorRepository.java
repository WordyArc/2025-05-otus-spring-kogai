package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Author;

@RepositoryRestResource(path = "authors", collectionResourceRel = "authors", itemResourceRel = "author")
public interface AuthorRepository extends JpaRepository<Author, Long> { }
