package ru.otus.hw.mongo.repositories;


import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.mongo.models.AuthorDocument;


public interface MongoAuthorRepository extends MongoRepository<AuthorDocument, String> { }
