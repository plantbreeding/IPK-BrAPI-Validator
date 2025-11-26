package org.brapi.brava.core.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Collection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Class for schema validation.
 */
public class SchemaValidator {

    private static final JsonSchemaFactory validator = JsonSchemaFactory.byDefault();
    private static final JsonNodeReader NODE_READER = new JsonNodeReader();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Validate an instance with a schema
     *
     * @param path           Path to the schema.
     * @param instanceString String with the JSON to be validated
     * @return Processing report of the validation.
     * @throws IOException         Thrown when reading the schema is not possible.
     * @throws ProcessingException Thrown when validating the instance.
     */
    public static ProcessingReport validate(String path, String instanceString) throws ProcessingException, IOException, CollectionNotFound {

        JsonNode instance = NODE_READER.fromInputStream(new ByteArrayInputStream(instanceString.getBytes(Charset.defaultCharset())));

        InputStream resource = SchemaValidator.class.getClassLoader().getResourceAsStream(path);

        if (resource != null) {
            final JsonSchema schemaNode = validator.getJsonSchema(mapper.readTree(resource));

            return schemaNode.validateUnchecked(instance, true);
            //Unchecked, deepCheck
        }
        else {
            throw new CollectionNotFound("No resource found with path: " + path);
        }
    }

}
