package ru.otus.hw.persistence.rdbms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.persistence.rdbms.model.Genre;

import java.util.Collection;
import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findAllByIdIn(Collection<Long> ids);
}
