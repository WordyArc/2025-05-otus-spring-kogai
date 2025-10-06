package ru.otus.hw.mongo.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.mongo.models.BookDocument;

public interface MongoBookRepository extends MongoRepository<BookDocument, ObjectId> { }
