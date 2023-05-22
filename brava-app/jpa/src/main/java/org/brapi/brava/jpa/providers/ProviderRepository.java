package org.brapi.brava.jpa.providers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProviderRepository extends CrudRepository<ProviderEntity, UUID>,
        PagingAndSortingRepository<ProviderEntity, UUID> {

  /**
   * Returns a {@link Page} of entities meeting the paging restriction provided in the {@link Pageable} object
   * and which contains name argument in the name ignoring the case
   *
   * @param name the name or partial name for the provider
   * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must not be
   *          {@literal null}.
   * @return a page of entities
   */
  Page<ProviderEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
