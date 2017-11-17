package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper class to get values from config file.
 */
public class Config {
    private static Properties prop = new Properties();

    protected static void init() {

        InputStream input = null;

        try {
            input = Config.class.getResourceAsStream("/config.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
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

}
