package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Genre;

import java.util.Collection;
import java.util.List;

@RepositoryRestResource(path = "genres", collectionResourceRel = "genres", itemResourceRel = "genre")
public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findAllByIdIn(Collection<Long> ids);
}
