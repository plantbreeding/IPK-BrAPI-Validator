package org.brapi.brava.jpa.resources;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProviderRepository extends CrudRepository<ProviderEntity, UUID>,
        PagingAndSortingRepository<ProviderEntity, UUID> {
}
