package org.brapi.brava.core.config;

import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Collection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionFactoryTest {

    private static final List<String> EXPECTED_VERSIONS = Arrays.asList("v1.0", "v1.1", "v1.2", "v1.3", "v2.0", "v2.1") ;

    private static CollectionFactory collectionFactory ;

    @BeforeAll
    static void setupClass() {
        collectionFactory = new CollectionFactory() ;
    }

    @Test
    void getCollectionNames() {
         assertEquals(EXPECTED_VERSIONS, collectionFactory.getCollectionNames(), "Versions are not as expected!") ;
    }

    @Test
    void getDefaultCollectionName() {
        assertEquals(EXPECTED_VERSIONS.get(EXPECTED_VERSIONS.size() - 1), collectionFactory.getDefaultCollectionName(), "Default version is not correct!") ;
    }

    @Test
    void getCollections() {
        EXPECTED_VERSIONS.forEach(versionName -> {
            try {
                Collection collection = collectionFactory.getCollection(versionName);

                assertNotNull(collection, "Collection is null");
            } catch (CollectionNotFound e) {
                fail(e.getMessage()) ;
            }
        }) ;

    }
}