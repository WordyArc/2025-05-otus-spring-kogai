package ru.otus.hw.mongo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Document("books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDocument {

    @Id
    private ObjectId id;

    private String title;

    @DBRef(lazy = false)
    private AuthorDocument author;

    @DBRef(lazy = true)
    private List<GenreDocument> genres = new ArrayList<>();

}
