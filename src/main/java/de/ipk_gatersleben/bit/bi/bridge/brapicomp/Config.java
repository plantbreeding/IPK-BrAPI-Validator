package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Arrays;

/**
 * Helper class to get values from config file.
 */
public class Config {
    private static final Logger LOGGER = LogManager.getLogger(Config.class);
    private static Properties prop = new Properties();

    protected static void init() {
        InputStream input = null;

        try {
            input = Config.class.getResourceAsStream("/config.properties");
            prop.load(input);
        } catch (NullPointerException e) {
            LOGGER.error("No /config.properties found on CLASSPATH.");
            throw e;
        } catch (IOException e) {
            LOGGER.error(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get value
     *
     * @param key Property name
     * @return Property value
     */
    public static String get(String key) {
        return prop.getProperty(key);
    }

    /**
     * Get all available BrAPI versions
     *
     * @return List of available BrAPI versions
     */
    public static List<String> getBrAPIVersions() {
        String[] versions = prop.getProperty("availableVersions").toString().split(",");
        List<String> versionsList = Arrays.asList(versions);
        return versionsList;
    }

}
