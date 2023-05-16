package org.brapi.brava.mongodb.providers;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProviderDocumentRepository extends MongoRepository<ProviderDocument, UUID> {
}
