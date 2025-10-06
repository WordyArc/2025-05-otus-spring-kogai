package ru.otus.hw.mongo.repositories;


import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.mongo.models.AuthorDocument;


public interface MongoAuthorRepository extends MongoRepository<AuthorDocument, ObjectId> {
}
