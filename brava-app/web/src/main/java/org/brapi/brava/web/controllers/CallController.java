package org.brapi.brava.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.service.ValidationService;
import org.brapi.brava.core.utils.ReportParser;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/test")
public class CallController {

    @Value("${org.brapi.brava.advancedMode}")
    boolean advancedMode ;
    private final ValidationService validationService ;

    public CallController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/call")
    public ValidationReport validate(@RequestParam("url") String url,
                                     @RequestParam Optional<String> accessToken,
                                     @RequestParam("brapiversion") Optional<String> version,
                                     @RequestParam Optional<Boolean> strict,
                                     @RequestParam Optional<String> authorizationMethod) {

        log.debug("New GET /call call.");

        try {
            if (url == null || url.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid url parameter");
            }

            String collectionName = version.orElse(validationService.getDefaultCollectionName()) ;

            if (!validationService.getCollectionNames().contains(collectionName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid version %s", collectionName));
            }

            Resource resource = new Resource(url, accessToken.orElse(null)) ;

            return validationService.validate(resource,
                    version.orElse(validationService.getDefaultCollectionName()),
                    strict.orElse(false),
                    advancedMode,
                    authorizationMethod.map(AuthorizationMethod::valueOf)) ;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("The provided URL '%s' was Malformed!", url), e);
        } catch (CollectionNotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown collection '%s', must be one of %s", url, validationService.getCollectionNames()), e);
        }  catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("The authorization method '%s' is unknown please use one of %s", url, Arrays.toString(AuthorizationMethod.values())), e);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Can not convert the report to json", e);
        }
    }
}
