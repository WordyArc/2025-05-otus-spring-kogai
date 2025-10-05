package ru.otus.hw.mongo.repositories;


import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.mongo.models.GenreDocument;

import java.util.Collection;
import java.util.List;

public interface MongoGenreRepository extends MongoRepository<GenreDocument, String> {
    List<GenreDocument> findAllByIdIn(Collection<String> ids);
}