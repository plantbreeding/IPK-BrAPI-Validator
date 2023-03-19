package org.brapi.brava.web;

import org.brapi.brava.core.config.CollectionFactory;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.core.validation.CallSuiteValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ValidationService {
    private CollectionFactory collectionFactory = new CollectionFactory();

    @Value("${org.brapi.brava.advancedMode}")
    private boolean advancedMode;

    public List<String> getCollectionNames() {
        return collectionFactory.getCollectionNames();
    }

    public String getDefaultCollectionName() {
        return collectionFactory.getDefaultCollectionName();
    }


    public SuiteReport validate(String url,
                                String accessToken,
                                String version,
                                boolean strict,
                                Optional<AuthorizationMethod> authorizationMethod) throws CollectionNotFound, MalformedURLException {

        CallSuiteValidator validator = new CallSuiteValidator(
                UUID.randomUUID().toString(),
                new Resource(url, accessToken),
                collectionFactory.getCollection(version),
                advancedMode);

        return validator.validate(!strict, true, authorizationMethod.orElse(AuthorizationMethod.BEARER_HEADER));
    }


}
