package org.brapi.brava.jpa.service;

import org.brapi.brava.core.model.Provider;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.data.dto.ResourceDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotFoundRuntimeException;
import org.brapi.brava.data.exceptions.EntityNotValidRuntimeException;
import org.brapi.brava.data.service.ResourceService;
import org.brapi.brava.jpa.providers.ProviderEntity;
import org.brapi.brava.jpa.providers.ProviderRepository;
import org.brapi.brava.jpa.resources.ResourceEntity;
import org.brapi.brava.jpa.resources.ResourceRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Profile("jpa")
@Service
public class JPAResourceService implements ResourceService {

    private final ResourceRepository resourceRepository ;
    private final ProviderRepository providerRepository ;

    public JPAResourceService(ResourceRepository resourceRepository,
                              ProviderRepository providerRepository) {
        this.resourceRepository = resourceRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    public Resource updateResource(String id, ResourceDTO resourceDTO) throws EntityNotFoundException {
        try {
            return resourceRepository.findById(UUID.fromString(id)).map(resourceEntity -> updateResourceAndSave(resourceEntity, resourceDTO)).
                    orElseThrow( () -> new EntityNotFoundRuntimeException(String.format("Can not find Resource with id : %s ", id))) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage()) ;
        }
    }

    @Override
    public Page<Resource> findAllResources(Pageable pageable) {
        return resourceRepository.findAll(pageable).map(this::convertToModel) ;
    }

    @Override
    public Resource findResource(String id) throws EntityNotFoundException {
        try {
            return convertToModel(resourceRepository.findById(UUID.fromString(id)).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Report with id : %s ", id))));
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    @Override
    public Resource createResource(ResourceDTO dto) {
        ResourceEntity entity = new ResourceEntity() ;

        updateResource(entity, dto) ;

        return convertToModel(resourceRepository.save(entity)) ;
    }

    @Override
    public Resource deleteResource(String id) throws EntityNotFoundException {
        try {
            ResourceEntity entity = resourceRepository.findById(UUID.fromString(id)).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Resource with id : %s ", id))) ;
            resourceRepository.delete(entity) ;
            return convertToModel(entity) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    private Resource updateResourceAndSave(ResourceEntity entity, ResourceDTO dto)  {

        updateResource(entity, dto) ;

        return convertToModel(resourceRepository.save(entity)) ;
    }

    private void updateResource(ResourceEntity entity, ResourceDTO dto) throws EntityNotValidRuntimeException {

        try {
            new URL(dto.getUrl()) ;
        } catch (MalformedURLException e) {
            throw new EntityNotValidRuntimeException(e.getMessage());
        }

        entity.setUrl(dto.getUrl());

        entity.setAuthorizationMethod(dto.getAuthorizationMethod());
        entity.setCertificate(dto.getCertificate());
        entity.setConfirmed(dto.isConfirmed());
        entity.setCollectionName(dto.getCollectionName());
        entity.setCrop(dto.getCrop());
        entity.setDescription(dto.getDescription());
        entity.setEmail(dto.getEmail());
        entity.setFrequency(dto.getFrequency());
        entity.setLogo(dto.getLogo());
        entity.setName(dto.getName());
        if (dto.getProviderId() != null) {
            entity.setProvider(providerRepository.findById(UUID.fromString(dto.getProviderId())).
                    orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Provider with id : %s ", dto.getProviderId()))));
        }
        entity.setPublic(dto.isPublic());
    }

    private Resource convertToModel(ResourceEntity entity) {
        Resource resource = null;
        try {
            resource = new Resource(entity.getId().toString(), entity.getUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        resource.setAuthorizationMethod(entity.getAuthorizationMethod());
        resource.setCertificate(entity.getCertificate());
        resource.setConfirmed(entity.isConfirmed());
        resource.setCollectionName(entity.getCollectionName());
        resource.setCrop(entity.getCrop());
        resource.setDescription(entity.getDescription());
        resource.setEmail(entity.getEmail());
        resource.setFrequency(entity.getFrequency());
        resource.setLogo(entity.getLogo());
        resource.setName(entity.getName());
        if (entity.getProvider() != null) {
            resource.setProvider(convertToModel(entity.getProvider()));
        }
        resource.setPublic(entity.isPublic());

        return resource ;
    }

    private Provider convertToModel(ProviderEntity entity) {
        Provider provider = new Provider(entity.getId().toString(), entity.getName()) ;

        provider.setDescription(entity.getDescription());
        provider.setLogo(entity.getLogo());
        provider.setEmail(entity.getEmail());

        return provider ;
    }
}
