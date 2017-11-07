package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used to gather information about a test made for a single endpoint. 
 * Uses the test name as input and stores endpoint and schema.
 */
public class Test {
	private String schema;
	private String endpoint;
	
	public Test(String name) throws IllegalArgumentException {
		findTest(name);
	}
	
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	
	/**
	 * Finds the test in the /tests.json docs.
	 * @param name Name of the test in the json
	 * @throws IllegalArgumentException Thrown when the test is not found.
	 */
	private void findTest(String name) throws IllegalArgumentException {
		
		ObjectMapper mapper = new ObjectMapper();
		InputStream is = this.getClass().getResourceAsStream("/tests.json");
		try {
			JsonNode testData = mapper.readTree(is);
			JsonNode testList = testData.get("tests");
			for (int i = 0; i < testList.size(); i++) {
				JsonNode test = testList.get(i);
				if (test.get("name").asText().equals(name)) {
					setSchema(test.get("pathToSchema").asText());
					setEndpoint(test.get("name").asText());
					return;
				}
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException();
	}
}
