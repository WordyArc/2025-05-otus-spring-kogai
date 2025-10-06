package ru.otus.hw.migration.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.migration.models.IdMappingDocument;

import java.util.Optional;

public interface IdMappingRepository extends MongoRepository<IdMappingDocument, ObjectId> {
    Optional<IdMappingDocument> findBySourceTypeAndSourceId(String srcType, String srcId);
}
