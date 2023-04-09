package org.brapi.brava.api ;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.service.ValidationService;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.data.service.ValidationReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.util.*;

@RestController
public class ValidationController {

    @Value("${org.brapi.brava.advancedMode}")
    boolean advancedMode ;

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

    @GetMapping("validate")
    public ValidationReport validate(String url,
                                Optional<String> accessToken,
                                Optional<String> collectionName,
                                Optional<Boolean> strict,
                                Optional<String> authorizationMethod) {

        try {

            Resource resource = new Resource(url, accessToken.orElse(null)) ;

            return validationReportService.save(validationService.validate(resource,
                    collectionName.orElse(validationService.getDefaultCollectionName()),
                    strict.orElse(false),
                    advancedMode,
                    authorizationMethod.map(AuthorizationMethod::valueOf))) ;

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("The provided URL '%s' was Malformed!", url), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("The authorization method '%s' is unknown please use one of %s", url, Arrays.toString(AuthorizationMethod.values())), e);
        } catch (CollectionNotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown collection '%s', must be one of %s", url, validationService.getCollectionNames()), e);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Can not convert the report to json", e);
        }
    }
}
