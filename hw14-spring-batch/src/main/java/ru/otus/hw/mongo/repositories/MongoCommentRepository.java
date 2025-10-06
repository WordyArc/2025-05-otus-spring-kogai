package ru.otus.hw.mongo.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.mongo.models.CommentDocument;

import java.util.List;

public interface MongoCommentRepository extends MongoRepository<CommentDocument, ObjectId> {
    List<CommentDocument> findAllByBookId(ObjectId bookId);
    
    void deleteByBookId(ObjectId bookId);
}