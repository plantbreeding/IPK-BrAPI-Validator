package org.brapi.brava.api;

import org.brapi.brava.core.model.Provider;
import org.brapi.brava.data.dto.ProviderDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.service.ProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProviderController {
    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping(path = "/providers")
    public Page<Provider> getAllProviders(Pageable pageable) {
        return providerService.findAllProviders(pageable);
    }

    @GetMapping(path = "/providers/{id}")
    public Provider getProvider(@PathVariable String id) {
        try {
            return providerService.findProvider(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Provider id : '%s' is not a valid UUID", id), e);
        }
    }

    @PutMapping(path = "/providers/{id}")
    public Provider putProvider(@PathVariable String id, @RequestBody ProviderDTO providerDTO) {
        try {
            return providerService.updateProvider(id, providerDTO);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Provider is not valid due to %s", e.getMessage()), e);
        } catch (EntityNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(path = "/providers")
    public Provider postProvider(@RequestBody ProviderDTO providerDTO) {
        try {
            return providerService.createProvider(providerDTO);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Provider is not valid due to %s", e.getMessage()), e);
        }
    }

    @DeleteMapping(path = "/providers/{id}")
    public Provider deleteProvider(@PathVariable String id) {
        try {
            return providerService.deleteProvider(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Provider is not valid due to %s", e.getMessage()), e);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format(e.getMessage()));
        }
    }
}
