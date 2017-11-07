package de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper functions for the resource classes. 
 *
 */
public class ResourceService {
	/**
	 * Finds a test collection file given a test collection name using the info stored in tests.json
	 * Mostly a way to prevent the user using any path when selecting a test.
	 * @param name Name of the collection as defined in tests.json/dataTests
	 * @return The path to the collection, relative to resources:/collections/.
	 * @throws IllegalArgumentException Raised when no test with the input name is found.
	 */
	public static String findTestCollection(String name) throws IllegalArgumentException {
		
		ObjectMapper mapper = new ObjectMapper();
		InputStream is = ResourceService.class.getResourceAsStream("/tests.json");
		try {
			JsonNode testData = mapper.readTree(is);
			JsonNode testList = testData.get("dataTests");
			for (int i = 0; i < testList.size(); i++) {
				JsonNode test = testList.get(i);
				if (test.get("name").asText().equals(name)) {
					return test.get("pathToCollection").asText();
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException();
	}
}
