package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.EndpointService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;

/**
 * Groups all user management related endpoints. Create/delete users and endpoints
 */
@Path("/ci")
@RequestScoped
public class ContinuousIntegration {

	private static final Logger LOGGER = Logger.getLogger(ContinuousIntegration.class.getName());
	

	
	/**
	 * Register new endpoint
	 * @param endp Json containing the endpoint's url.
	 * @return Json message.
	 */
	@POST
	@Path("/endpoints")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createEndpoint(@Context HttpHeaders headers,
									Endpoint endp) {

		LOGGER.log(Level.FINER, "New POST /ci/endpoints call.");
	
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		
		try {
			Endpoint e = EndpointService.getEndpointWithEmailAndUrl(endp.getEmail(), endp.getUrl());
			if (e != null) {
				String e2 = JsonMessageManager.jsonMessage(400, "Url already in use", 4021); 
				return Response.status(Status.BAD_REQUEST).entity(e2).build();
			}
			endpointDao.create(endp);
			
			return Response.status(Status.CREATED).entity(JsonMessageManager.jsonMessage(200, "success", 2000)).build();
		} catch (SQLException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5020); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}

	/**
	 * Delete an endpoint. We assume if they have the Id then they can delete it (for now).
	 * @param endpointId Endpoint id
	 * @param apiKey User's API key
	 * @return json message
	 */
	@DELETE
	@Path("/endpoints/{endpointId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteEndpoint(@PathParam("endpointId") String endpointId,
									@Context HttpHeaders headers) {

		LOGGER.log(Level.FINER, "New DELETE /ci/endpoints call. EndpointId: " + endpointId);
	
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		
		try {
			
			boolean del = EndpointService.deleteEndpointWithId(endpointId);
			if (!del) {
				String e2 = JsonMessageManager.jsonMessage(404, "endpoint not found", 4021); 
				return Response.status(Status.NOT_FOUND).entity(e2).build();
			}

			String json = JsonMessageManager.jsonMessage(200, "endpoint deleted", 2020);
			
			return Response.ok().entity(json).build();
		} catch (SQLException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5020); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}

}
