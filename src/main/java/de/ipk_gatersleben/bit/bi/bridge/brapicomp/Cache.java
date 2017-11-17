package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.util.HashMap;
import java.util.Map;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.RestAssuredRequest;

/**
 * Application cache
 *
 */
public class Cache {
	// Stores RestAssured responses.
	// The key is the method followed by the url.
	// The value is the response and the timestamp.
	private static final Map<String, RestAssuredRequest> requests = new HashMap<String, RestAssuredRequest>();

	public Cache() {}
	
	public static void addRequest(String key, RestAssuredRequest value) {
		requests.put(key, value);
	}
	
	public static RestAssuredRequest getRequest(String key) {
		return requests.get(key);
	}
}
