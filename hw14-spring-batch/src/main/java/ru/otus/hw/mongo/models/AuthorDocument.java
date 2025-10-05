package ru.otus.hw.mongo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("authors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDocument {

    @Id
    private String id;

    private String fullName;
}