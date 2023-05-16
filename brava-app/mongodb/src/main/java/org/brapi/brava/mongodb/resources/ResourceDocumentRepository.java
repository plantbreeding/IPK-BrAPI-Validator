package org.brapi.brava.mongodb.resources;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ResourceDocumentRepository extends MongoRepository<ResourceDocument, UUID> {
}
