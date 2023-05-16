package org.brapi.brava.jpa.service;

import org.brapi.brava.core.model.Provider;
import org.brapi.brava.data.dto.ProviderDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotFoundRuntimeException;
import org.brapi.brava.data.service.ProviderService;
import org.brapi.brava.jpa.providers.ProviderEntity;
import org.brapi.brava.jpa.providers.ProviderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Profile("jpa")
@Service
public class JPAProviderService implements ProviderService {
    private final ProviderRepository providerRepository ;

    public JPAProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    public Page<Provider> findAllProviders(Pageable pageable) {
        return providerRepository.findAll(pageable).map(this::convertToModel) ;
    }

    @Override
    public Provider findProvider(String id) throws EntityNotFoundException {
        try {
            return convertToModel(providerRepository.findById(UUID.fromString(id)).orElseThrow( () -> new EntityNotFoundRuntimeException(String.format("Can not find Provider with id : %s ", id)))) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage()) ;
        }
    }

    @Override
    public Provider createProvider(ProviderDTO dto) {
        ProviderEntity entity = new ProviderEntity() ;

        updateProvider(entity, dto) ;

        return convertToModel(providerRepository.save(entity)) ;
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
            ProviderEntity entity = providerRepository.findById(UUID.fromString(id)).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Provider with id : %s ", id))) ;
            providerRepository.delete(entity) ;
            return convertToModel(entity) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }


    private void updateProvider(ProviderEntity entity, ProviderDTO dto) {
        entity.setDescription(dto.getDescription());
        entity.setEmail(dto.getEmail());
        entity.setLogo(dto.getLogo());
        entity.setName(dto.getName());
    }

    private Provider updateProviderAndSave(ProviderEntity entity, ProviderDTO dto) {
        updateProvider(entity, dto) ;

        return convertToModel(providerRepository.save(entity)) ;
    }

    private Provider convertToModel(ProviderEntity entity) {
        Provider provider = new Provider(entity.getId().toString(), entity.getName()) ;

        provider.setDescription(entity.getDescription());
        provider.setLogo(entity.getLogo());
        provider.setEmail(entity.getEmail());

        return provider ;
    }
}
