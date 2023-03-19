package org.brapi.brava.api;

import org.brapi.brava.core.config.CollectionFactory;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.core.validation.CallSuiteValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ValidationService {

    private final CollectionFactory collectionFactory ;

    public ValidationService(CollectionFactory collectionFactory) {
        this.collectionFactory = collectionFactory;
    }

    public String getDefaultCollectionName() {
        return collectionFactory.getDefaultCollectionName() ;
    }

    public List<String> getCollectionNames() {
        return collectionFactory.getCollectionNames() ;
    }

    public SuiteReport validate(Resource resource,
                                String collectionName,
                                boolean advancedMode,
                                boolean strict,
                                Optional<AuthorizationMethod> authorizationMethod) throws CollectionNotFound {
        CallSuiteValidator validator = new CallSuiteValidator(
                UUID.randomUUID().toString(),
                resource,
                collectionFactory.getCollection(collectionName),
                advancedMode);

        boolean allowAdditional = !strict;

        return validator.validate(allowAdditional, true, authorizationMethod.orElse(AuthorizationMethod.BEARER_HEADER));
    }
}
