package ru.otus.hw.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.jpa.models.Genre;

import java.util.Collection;
import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findAllByIdIn(Collection<Long> ids);
}
