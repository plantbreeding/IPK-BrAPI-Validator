package org.brapi.brava.data.service;

import org.brapi.brava.core.model.Resource;
import org.brapi.brava.data.dto.ResourceDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotValidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for searching for and persisting Resources
 */
public interface ResourceService {
    Resource updateResource(String id, ResourceDTO resourceDTO) throws EntityNotFoundException, EntityNotValidException;

    Page<Resource> findAllResources(Pageable pageable);

    Resource findResource(String id) throws EntityNotFoundException;

    Resource createResource(ResourceDTO resourceDTO) throws EntityNotValidException ;

    Resource deleteResource(String id) throws EntityNotFoundException;
}
