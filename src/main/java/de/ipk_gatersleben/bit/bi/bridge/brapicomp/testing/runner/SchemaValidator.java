package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * Class for schema validation.
 *
 */
public class SchemaValidator {
	
	private JsonSchemaFactory validator;
	private static final JsonNodeReader NODE_READER = new JsonNodeReader();
	
	public SchemaValidator() {
		this.validator = JsonSchemaFactory.byDefault();
	}
	
	/**
	 * Validate an instance with a schema
	 * @param path Path to the schema.
	 * @param instanceString String with the JSON to be validated
	 * @return Processing report of the validation.
	 * @throws IOException Thrown when reading the schema is not possible.
	 * @throws ProcessingException Thrown when validating the instance.
	 */
	public ProcessingReport validate(String path, String instanceString) throws ProcessingException, IOException {
		

		JsonNode instance = NODE_READER.fromInputStream(new ByteArrayInputStream(instanceString.getBytes(Charset.defaultCharset())));
		final JsonSchema schemaNode = validator.getJsonSchema("resource:" + path);
		
		ProcessingReport r = schemaNode.validateUnchecked(instance, true); //Unchecked, deepCheck
		return r;
	}

}
