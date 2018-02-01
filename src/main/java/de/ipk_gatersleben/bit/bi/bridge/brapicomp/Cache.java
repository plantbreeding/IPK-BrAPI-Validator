package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.util.HashMap;
import java.util.Map;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.RestAssuredRequest;

/**
 * Application cache
 *
 */
public class Cache {
	
	private static final Map<String, String> cache = new HashMap<String, String>();

	public Cache() {}
	
	public static void addToCache(String key, String value) {
		cache.put(key, value);
	}
	
	public static String getFromCache(String key) {
		return cache.get(key);
	}
}
