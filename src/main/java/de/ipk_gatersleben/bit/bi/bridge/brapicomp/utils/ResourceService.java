package de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.internal.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper functions for the resource classes.
 */
public class ResourceService {
    private static final Logger LOGGER = Logger.getLogger(ResourceService.class.getName());

    /**
     * Finds a test collection file given a test collection name using the info stored in tests.json
     * Mostly a way to prevent the user using any path when selecting a test.
     *
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
            LOGGER.warning("Test has invalid JSON");
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.warning("IOException accessing file");

            e.printStackTrace();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns user and password from BASIC auth header.
     *
     * @param headers
     * @return String array with usernamme and password
     */
    public static String[] getAuth(HttpHeaders headers) {

        //Fetch authorization header
        final List<String> authorization = headers.getRequestHeader("Authorization");

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.get(0).replaceFirst("Basic ", "");

        //Decode username and password
        String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
        ;

        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        String password = "";
        try {
            password = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            return new String[]{"", username};
        }
        return new String[]{username, password};
    }

    /**
     * Get api key (BASIC password) from request headers.
     *
     * @param headers
     * @return
     */
    public static String getApiKey(HttpHeaders headers) {
        String[] auth = getAuth(headers);
        if (auth == null || auth.length != 2) {
            return null;
        }
        return auth[1];
    }
}
