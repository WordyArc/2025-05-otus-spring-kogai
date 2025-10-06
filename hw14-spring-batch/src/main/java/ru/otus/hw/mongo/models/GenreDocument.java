package ru.otus.hw.mongo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDocument {

    @Id
    private ObjectId id;

    private String name;

}
