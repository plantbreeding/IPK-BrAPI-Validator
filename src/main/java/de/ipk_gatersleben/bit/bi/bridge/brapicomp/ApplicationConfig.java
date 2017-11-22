package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Servlet application config.
 */
@ApplicationPath("/")
public class ApplicationConfig extends ResourceConfig {
    private static final Logger LOGGER = Logger.getLogger(ApplicationConfig.class.getName());

    public ApplicationConfig(@Context ServletContext servletContext) throws SQLException, IOException {
        LOGGER.log(Level.FINE, "Server initialized.");

        packages("de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources.AdminResource");
        packages("de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources.SimpleTests");
        packages("de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources.Users");

    }


}
