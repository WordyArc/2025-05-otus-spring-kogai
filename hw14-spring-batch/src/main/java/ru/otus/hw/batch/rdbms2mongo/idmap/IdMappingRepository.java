package ru.otus.hw.batch.rdbms2mongo.idmap;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IdMappingRepository extends MongoRepository<IdMappingDocument, ObjectId> {
    Optional<IdMappingDocument> findBySourceTypeAndSourceId(String srcType, String srcId);
}
