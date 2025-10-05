package ru.otus.hw.mongo.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.mongo.models.CommentDocument;

import java.util.List;

public interface MongoCommentRepository extends MongoRepository<CommentDocument, String> {
    List<CommentDocument> findAllByBookId(String bookId);
    
    void deleteByBookId(String bookId);
}