package org.brapi.brava.mongodb.providers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProviderDocumentRepository extends MongoRepository<ProviderDocument, UUID> {

  Page<ProviderDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
