package ru.otus.hw.batch.rdbms2mongo.idmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("id_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdMappingDocument {
    @Id
    private ObjectId id;

    private String sourceType;

    private String sourceId;

    private ObjectId targetId;
}
