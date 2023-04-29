package org.brapi.brava.api ;

import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.service.ValidationService;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.service.ValidationException;
import org.brapi.brava.data.service.ValidationReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@RestController
public class ValidationController {

    @Value("${org.brapi.brava.advancedMode}")
    boolean advancedMode ;

    @Value("${org.brapi.brava.asyncDefault}")
    boolean asyncDefault ;

    boolean strictDefault ;

    private final ValidationService validationService ;

    private final ValidationReportService validationReportService ;

    public ValidationController(ValidationService validationService,
                                ValidationReportService validationReportService) {
        this.validationService = validationService;
        this.validationReportService = validationReportService;

    }

    @GetMapping("collectionNames")
    public List<String> collectionNames() {
        return validationService.getCollectionNames() ;
    }

    @PostMapping("validate")
    public ValidationReport executeValidation(@RequestBody URLValidationRequest request) {

        try {
            new URL(request.getUrl()) ; // check if valid URL

            if (request.getAsync() != null ? request.getAsync() : asyncDefault) {
                return validationReportService.submitValidation(request.getUrl(),
                        request.getCollectionName() != null ? request.getCollectionName() : validationService.getDefaultCollectionName(),
                        advancedMode,
                        request.getStrict() != null ? request.getStrict() : strictDefault,
                        request.getAuthorizationMethod() != null ? AuthorizationMethod.valueOf(request.getAuthorizationMethod()) : AuthorizationMethod.NONE,
                        request.getAccessToken() != null ? request.getAccessToken() : null);
            } else {
                return validationReportService.executeValidation(request.getUrl(),
                        request.getCollectionName() != null ? request.getCollectionName() : validationService.getDefaultCollectionName(),
                        advancedMode,
                        request.getStrict() != null ? request.getStrict() : strictDefault,
                        request.getAuthorizationMethod() != null ? AuthorizationMethod.valueOf(request.getAuthorizationMethod()) : AuthorizationMethod.NONE,
                        request.getAccessToken() != null ? request.getAccessToken() : null);
            }

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("The provided URL '%s' was Malformed!", request.getUrl()), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/resources/{resourceId}/validate")
    public ValidationReport validateResource(@PathVariable String resourceId,
                                             @RequestBody Optional<ValidationRequest> request) {

        try {
            if (request.map(ValidationRequest::getAsync).orElse(asyncDefault)) {
                return validationReportService.submitValidation(resourceId,
                        advancedMode,
                        request.map(ValidationRequest::getStrict).orElse(strictDefault),
                        request.map(ValidationRequest::getAccessToken).orElse(null)) ;
            } else {
                return validationReportService.executeValidation(resourceId,
                        advancedMode,
                        request.map(ValidationRequest::getStrict).orElse(strictDefault),
                        request.map(ValidationRequest::getAccessToken).orElse(null)) ;
            }

        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
