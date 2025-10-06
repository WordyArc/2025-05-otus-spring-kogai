package ru.otus.hw.persistence.mongo.repository;


import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.persistence.mongo.model.GenreDocument;

import java.util.Collection;
import java.util.List;

public interface MongoGenreRepository extends MongoRepository<GenreDocument, ObjectId> {
    List<GenreDocument> findAllByIdIn(Collection<ObjectId> ids);
}