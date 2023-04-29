package org.brapi.brava.data.service;

import org.brapi.brava.core.model.Provider;
import org.brapi.brava.data.dto.ProviderDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for searching for and persisting Resources
 */
public interface ProviderService {
    Provider updateProvider(String id, ProviderDTO providerDTO) throws EntityNotFoundException;

    Page<Provider> findAllProviders(Pageable pageable);

    Provider findProvider(String id) throws EntityNotFoundException ;

    Provider createProvider(ProviderDTO providerDTO);
}
