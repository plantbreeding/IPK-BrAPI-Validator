package org.brapi.brava.api ;

import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
public class ValidationController {

    @Value("${org.brapi.brava.api.advancedMode}")
    boolean advancedMode ;

    private final ValidationService validationService ;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("collectionNames")
    public List<String> collectionNames() {
        return validationService.getCollectionNames() ;
    }

    @GetMapping("validate")
    public SuiteReport validate(String url,
                                Optional<String> accessToken,
                                Optional<String> collectionName,
                                Optional<Boolean> strict,
                                Optional<String> authorizationMethod) {

        try {
            return validationService.validate(new Resource(url, accessToken.orElse(null)),
                    collectionName.orElse(validationService.getDefaultCollectionName()),
                    strict.orElse(false),
                    advancedMode,
                    authorizationMethod.map(AuthorizationMethod::valueOf)) ;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("The provided URL '%s' was Malformed!", url), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("The authorization method '%s' is unknown please use one of %s", url, Arrays.toString(AuthorizationMethod.values())), e);
        } catch (CollectionNotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Unknown collection '%s', must be one of %s", url, validationService.getCollectionNames()), e);
        }
    }
}
