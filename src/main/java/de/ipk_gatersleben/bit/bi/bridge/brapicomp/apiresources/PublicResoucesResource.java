package de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources;

import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.process.internal.RequestScoped;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.ResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ApiResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;

@Path("/public")
@RequestScoped
public class PublicResoucesResource {
	
	private static final Logger LOGGER = LogManager.getLogger(PublicResoucesResource.class.getName());
	
	
    /**
     * Get all public endpoints and their stats
     *
     * @return JSON with endpoint data
     */
    @GET
    @Path("/resources")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resources() {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_IMPLEMENTED).build();
        }

        LOGGER.debug("New GET /public/resources");
        
        try {
        	List<Resource> trl = ResourceService.getAllPublicEndpoints();
        	ApiResourceService.saveStat("/public/resources");
        	return Response.ok().entity(trl).build();
        	
        } catch (SQLException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5401);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }


	
}
