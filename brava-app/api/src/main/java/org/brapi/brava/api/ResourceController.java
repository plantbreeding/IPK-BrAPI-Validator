package org.brapi.brava.api;

import org.brapi.brava.core.model.Resource;
import org.brapi.brava.data.dto.ResourceDTO;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotValidException;
import org.brapi.brava.data.service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ResourceController {
    private final ResourceService resourceService ;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping(path = "/resources")
    public Page<Resource> getAllResources(Pageable pageable) {
        return resourceService.findAllResources(pageable);
    }

    @GetMapping(path = "/resources/{id}")
    public Resource getResource(@PathVariable String id) {
        try {
            return resourceService.findResource(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Resource id : '%s' is not a valid UUID", id), e);
        }
    }

    @PutMapping(path = "/resources/{id}")
    public Resource putResource(@PathVariable String id, @RequestBody ResourceDTO resourceDTO) {
        try {
            return resourceService.updateResource(id, resourceDTO);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Resource is not valid due to %s", e.getMessage()), e);
        } catch (EntityNotFoundException | EntityNotValidException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format(e.getMessage()));
        }
    }

    @PostMapping(path = "/resources")
    public Resource postResource(@RequestBody ResourceDTO resourceDTO) {
        try {
            return resourceService.createResource(resourceDTO);
        } catch (IllegalArgumentException | EntityNotValidException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Resource is not valid due to %s", e.getMessage()), e);
        }
    }
}
