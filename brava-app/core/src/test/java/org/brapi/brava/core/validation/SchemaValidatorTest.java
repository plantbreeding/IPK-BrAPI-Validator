package org.brapi.brava.core.validation;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

class SchemaValidatorTest {

    //@Test
    void validate() {
        SchemaValidator schemaValidator = new SchemaValidator() ;

        URI uri = null;
        try {
            uri = ClassLoader.getSystemResource("response/v1/metadata.json").toURI();
            String json = Files.readString(Paths.get(uri));

            schemaValidator.validate("schemas/v1/metadata.json", json) ;
        } catch (URISyntaxException | IOException | ProcessingException e) {
            e.printStackTrace();

            fail(e.getMessage()) ;
        }
    }
}