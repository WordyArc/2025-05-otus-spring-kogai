package ru.otus.hw.persistence.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.persistence.mongo.model.BookDocument;

public interface MongoBookRepository extends MongoRepository<BookDocument, ObjectId> { }
