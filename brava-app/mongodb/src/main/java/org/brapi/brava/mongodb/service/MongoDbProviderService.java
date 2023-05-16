package org.brapi.brava.mongodb.service;

import org.brapi.brava.core.model.Provider;
import org.brapi.brava.data.dto.ProviderDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotFoundRuntimeException;
import org.brapi.brava.data.service.ProviderService;
import org.brapi.brava.mongodb.providers.ProviderDocument;
import org.brapi.brava.mongodb.providers.ProviderDocumentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Profile("mongodb")
@Service
public class MongoDbProviderService implements ProviderService {

    private final ProviderDocumentRepository providerRepository;

    public MongoDbProviderService(ProviderDocumentRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    public Page<Provider> findAllProviders(Pageable pageable) {
        return providerRepository.findAll(pageable).map(this::convertToModel);
    }

    @Override
    public Provider findProvider(String id) throws EntityNotFoundException {
        try {
            return convertToModel(providerRepository.findById(UUID.fromString(id)).orElseThrow( () -> new EntityNotFoundRuntimeException(String.format("Can not find Provider with id : %s ", id)))) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    @Override
    public Provider createProvider(ProviderDTO dto) {
        ProviderDocument document = new ProviderDocument() ;

        updateProvider(document, dto) ;

        return convertToModel(providerRepository.save(document)) ;
    }

    @Override
    public Provider updateProvider(String id, ProviderDTO providerDTO) throws EntityNotFoundException {
        try {
            return providerRepository.findById(UUID.fromString(id)).map(providerEntity -> updateProviderAndSave(providerEntity, providerDTO)).
                    orElseThrow( () -> new EntityNotFoundRuntimeException(String.format("Can not find Provider with id : %s ", id))) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage()) ;
        }
    }

    @Override
    public Provider deleteProvider(String id) throws EntityNotFoundException {
        try {
            ProviderDocument entity = providerRepository.findById(UUID.fromString(id)).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Provider with id : %s ", id))) ;
            providerRepository.delete(entity) ;
            return convertToModel(entity) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }


    private void updateProvider(ProviderDocument document, ProviderDTO dto) {
        document.setDescription(dto.getDescription());
        document.setEmail(dto.getEmail());
        document.setLogo(dto.getLogo());
        document.setName(dto.getName());
    }

    private Provider updateProviderAndSave(ProviderDocument entity, ProviderDTO dto) {
        updateProvider(entity, dto) ;

        return convertToModel(providerRepository.save(entity)) ;
    }

    private Provider convertToModel(ProviderDocument providerDocument) {
        Provider provider = new Provider(providerDocument.getId().toString(), providerDocument.getName()) ;

        provider.setDescription(providerDocument.getDescription());
        provider.setEmail(providerDocument.getEmail());
        provider.setLogo(providerDocument.getLogo());

        return provider ;
    }

}
