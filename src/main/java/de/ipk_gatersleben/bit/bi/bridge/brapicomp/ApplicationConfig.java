package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Servlet application config.
 */
@ApplicationPath("/")
public class ApplicationConfig extends ResourceConfig {
    private static final Logger LOGGER = LogManager.getLogger(ApplicationConfig.class.getName());

    public ApplicationConfig(@Context ServletContext servletContext) throws SQLException, IOException {
        LOGGER.info("Server initialized.");
    }


}
