package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Context;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;


/**
 * Servlet application config.
 */
@ApplicationPath("/")
public class ApplicationConfig extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class.getName());

    public ApplicationConfig(@Context ServletContext servletContext) throws SQLException, IOException {
        LOGGER.info("Server initialized.");
    }


}
