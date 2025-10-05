package ru.otus.hw.mongo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDocument {

    @Id
    private String id;

    private String text;

    private String bookId;

    private LocalDateTime createdAt;
    
    @Transient
    private BookDocument book;

}
