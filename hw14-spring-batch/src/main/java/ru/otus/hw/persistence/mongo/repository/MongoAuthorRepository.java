package ru.otus.hw.persistence.mongo.repository;


import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.persistence.mongo.model.AuthorDocument;


public interface MongoAuthorRepository extends MongoRepository<AuthorDocument, ObjectId> {
}
