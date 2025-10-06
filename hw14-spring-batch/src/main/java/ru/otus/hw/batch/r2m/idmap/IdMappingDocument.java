package ru.otus.hw.batch.r2m.idmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
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
