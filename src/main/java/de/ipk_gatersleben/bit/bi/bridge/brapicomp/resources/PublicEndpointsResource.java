package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.process.internal.RequestScoped;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.TemplateHTML;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.EndpointService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReportService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

@Path("/public")
@RequestScoped
public class PublicEndpointsResource {
	
	private static final Logger LOGGER = LogManager.getLogger(PublicEndpointsResource.class.getName());
	
	
    /**
     * Get all public endpoints and their stats
     *
     * @return JSON with endpoint data
     */
    @GET
    @Path("/endpoints")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeFrequency(@Context HttpHeaders headers) {

        LOGGER.debug("New GET /public/endpoints");
        
        try {
        	
        	List<TestReport> trl = TestReportService.getAllPublicEndpointLastReport();
        	return Response.ok().entity(trl).build();
        	
        } catch (SQLException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5401);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
	
}
