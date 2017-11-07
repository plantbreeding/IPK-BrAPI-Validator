package de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils;

/**
 * Generate Json messages and errors.
 */
public class JsonMessageManager {
	/**
	 * Generate Json message
	 * @param status Http status code.
	 * @param message Message explaining the problem
	 * @param code (Hopefully) unique code to locate in code.
	 * @return Json string
	 */
	public static String jsonMessage(int status, String message, int code) {
		String e = "{\"status\":" + status 
				+ ", \"message\":\"" + message 
				+ "\", \"code\":"+ code + "}";
		return e;
	}
	
	/**
	 * Generate "data" json structure
	 * @param data Data to be included
	 * @return Json string
	 */
	public static String jsonData(String data) {
		String e = "{\"data\":" + data + "}";
		return e;
	}
}
