package ru.otus.hw.migration.models;

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
@CompoundIndexes({
        @CompoundIndex(name = "sourceType_sourceId_unique",
                def = "{'sourceType': 1, 'sourceId': 1}", unique = true)
})
public class IdMappingDocument {
    @Id
    private ObjectId id;

    private String sourceType;

    private String sourceId;

    private ObjectId targetId;
}
