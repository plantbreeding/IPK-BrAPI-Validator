package org.brapi.brava.mongodb.service;

import org.brapi.brava.core.model.Provider;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.data.dto.ResourceDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotFoundRuntimeException;
import org.brapi.brava.data.exceptions.EntityNotValidRuntimeException;
import org.brapi.brava.data.service.ResourceService;
import org.brapi.brava.mongodb.providers.ProviderDocument;
import org.brapi.brava.mongodb.providers.ProviderDocumentRepository;
import org.brapi.brava.mongodb.resources.ResourceDocument;
import org.brapi.brava.mongodb.resources.ResourceDocumentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Profile("mongodb")
@Service
public class MongoDbResourceService implements ResourceService {

    private final ResourceDocumentRepository resourceRepository ;
    private final ProviderDocumentRepository providerRepository ;

    public MongoDbResourceService(ResourceDocumentRepository resourceRepository,
                                  ProviderDocumentRepository providerRepository) {
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
        ResourceDocument document = new ResourceDocument() ;

        updateResource(document, dto) ;

        return convertToModel(resourceRepository.save(document)) ;
    }

    @Override
    public Resource deleteResource(String id) throws EntityNotFoundException {
        try {
            ResourceDocument document = resourceRepository.findById(UUID.fromString(id)).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Resource with id : %s ", id))) ;
            resourceRepository.delete(document) ;
            return convertToModel(document) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    private Resource updateResourceAndSave(ResourceDocument document, ResourceDTO dto)  {

        updateResource(document, dto) ;

        return convertToModel(resourceRepository.save(document)) ;
    }

    private void updateResource(ResourceDocument entity, ResourceDTO dto) throws EntityNotValidRuntimeException {

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
            ProviderDocument provider = providerRepository.findById(UUID.fromString(dto.getProviderId())).
                    orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Provider with id : %s ", dto.getProviderId())));

            entity.setProviderId(provider.getId()) ;
        }
        entity.setPublic(dto.isPublic());
    }

    private Resource convertToModel(ResourceDocument document) {
        Resource resource = null;
        try {
            resource = new Resource(document.getId().toString(), document.getUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        resource.setAuthorizationMethod(document.getAuthorizationMethod());
        resource.setCertificate(document.getCertificate());
        resource.setConfirmed(document.isConfirmed());
        resource.setCollectionName(document.getCollectionName());
        resource.setCrop(document.getCrop());
        resource.setDescription(document.getDescription());
        resource.setEmail(document.getEmail());
        resource.setFrequency(document.getFrequency());
        resource.setLogo(document.getLogo());
        resource.setName(document.getName());
        if (document.getProviderId() != null) {
            resource.setProvider(convertToModel(providerRepository.findById(document.getProviderId()).orElse(null)));
        }
        resource.setPublic(document.isPublic());

        return resource ;
    }

    private Provider convertToModel(ProviderDocument document) {
        Provider provider = new Provider(document.getId().toString(), document.getName()) ;

        provider.setDescription(document.getDescription());
        provider.setLogo(document.getLogo());
        provider.setEmail(document.getEmail());

        return provider ;
    }
}
