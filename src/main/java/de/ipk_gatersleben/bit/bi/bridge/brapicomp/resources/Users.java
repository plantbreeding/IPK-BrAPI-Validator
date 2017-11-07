package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.User;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.UserService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ResourceService;

/**
 * Groups all user management related endpoints. Create/delete users and endpoints
 */
@Path("/users")
@RequestScoped
public class Users {

	private static final Logger LOGGER = Logger.getLogger(Users.class.getName());
	
	/**
	 * Create a user in the database.
	 * @param u Expects Json with User attributes: username and email.
	 * @return User data
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(User u) {

		LOGGER.log(Level.FINER, "New POST /users call. username: " + u.getUsername());
	
		Dao<User, UUID> userDao = DataSourceManager.getDao(User.class);
		
		try {
			if (UserService.usernameExists(u.getUsername())) {
				String e = JsonMessageManager.jsonMessage(400, "username already in use", 4010); 
				return Response.status(Status.BAD_REQUEST).entity(e).build();
			}
			u.generateApiKey();
			userDao.create(u);
			ObjectMapper mapper = new ObjectMapper();
			
			String json = mapper.writeValueAsString(u);
			
			return Response.status(Status.CREATED).entity(json).build();
		} catch (SQLException | JsonProcessingException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5010); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}
	
	/**
	 * Delete user
	 * @param username Username of the user to be deleted
	 * @param apiKey Apikey of the user to be deleted
	 * @return Response with Json message
	 */
	@DELETE
	@Path("/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUser(@PathParam("username") String username, @Context HttpHeaders headers) {

		LOGGER.log(Level.FINER, "New DELETE /users call. username: " + username);
	
		Dao<User, UUID> userDao = DataSourceManager.getDao(User.class);
		
		try {
			User u = UserService.getUser(username);
			if (u == null) {
				String e = JsonMessageManager.jsonMessage(404, "user not found", 4020); 
				return Response.status(Status.NOT_FOUND).entity(e).build();
			}
			
			String apiKey = ResourceService.getApiKey(headers);
			
			if (apiKey == null || !u.checkApiKey(apiKey)) {
				String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4021); 
				return Response.status(Status.UNAUTHORIZED).entity(e).build();
			}
			EndpointService.deleteAllUserEndpoints(u);
			UserService.deleteUser(u);
			
			String json = JsonMessageManager.jsonMessage(200, "user deleted", 2020);
			return Response.ok().entity(json).build();
			
		} catch (SQLException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5010); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}


	/**
	 * Get all endpoints from a user
	 * @param username Username
	 * @param apiKey User's API key
	 * @return Json with all endpoint's info
	 */
	@GET
	@Path("/{username}/endpoints")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEndpoints(@PathParam("username") String username, @Context HttpHeaders headers) {

		LOGGER.log(Level.FINER, "New GET /user/{userID}/endpoints call. userId: " + username);
		
		try {
			User u = UserService.getUser(username);
			if (u == null) {
				String e = JsonMessageManager.jsonMessage(404, "user not found", 4020); 
				return Response.status(Status.NOT_FOUND).entity(e).build();
			}
			String apiKey = ResourceService.getApiKey(headers);
			if (apiKey == null || !u.checkApiKey(apiKey)) {
				String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4021); 
				return Response.status(Status.UNAUTHORIZED).entity(e).build();
			}
			List<Endpoint> l = EndpointService.getEndpointsFromUser(u);

			ObjectMapper mapper = new ObjectMapper();
			
			String json = mapper.writeValueAsString(l);
			json = JsonMessageManager.jsonData(json);
			return Response.ok().entity(json).build();
		} catch (SQLException | JsonProcessingException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5020); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}
	
	/**
	 * Create new endpoint
	 * @param username Username
	 * @param apiKey User's API key
	 * @param endp Json containing the endpoint's url.
	 * @return Json message.
	 */
	@POST
	@Path("/{username}/endpoints")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createEndpoint(@PathParam("username") String username,
									@Context HttpHeaders headers,
									Endpoint endp) {

		LOGGER.log(Level.FINER, "New POST /user/{username}/endpoints call. userId: " + username);
	
		Dao<User, UUID> userDao = DataSourceManager.getDao(User.class);
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		
		try {
			User u = UserService.getUser(username);
			if (u == null) {
				String e = JsonMessageManager.jsonMessage(404, "user not found", 4020); 
				return Response.status(Status.NOT_FOUND).entity(e).build();
			}
			String apiKey = ResourceService.getApiKey(headers);
			if (apiKey == null || !u.checkApiKey(apiKey)) {
				String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4021); 
				return Response.status(Status.UNAUTHORIZED).entity(e).build();
			}
			
			Endpoint e = EndpointService.getUserEndpointWithUrl(u, endp.getUrl());
			if (e != null) {
				String e2 = JsonMessageManager.jsonMessage(400, "url already in use", 4021); 
				return Response.status(Status.BAD_REQUEST).entity(e2).build();
			}
			endp.setUser(u);
			endpointDao.create(endp);
			ObjectMapper mapper = new ObjectMapper();
			
			String json = mapper.writeValueAsString(endp);
			
			return Response.status(Status.CREATED).entity(json).build();
		} catch (SQLException | JsonProcessingException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5020); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}

	/**
	 * Delete an endpoint
	 * @param username Username
	 * @param endpointId Endpoint id
	 * @param apiKey User's API key
	 * @return json message
	 */
	@DELETE
	@Path("/{username}/endpoints/{endpointId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteEndpoint(@PathParam("username") String username,
									@PathParam("endpointId") int endpointId,
									@Context HttpHeaders headers) {

		LOGGER.log(Level.FINER, "New DELETE /user/{userID}/endpoints call. userId: " + username 
				+ " endpointUrl: " + endpointId);
	
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		
		try {
			User u = UserService.getUser(username);
			if (u == null) {
				String e = JsonMessageManager.jsonMessage(404, "user not found", 4020); 
				return Response.status(Status.NOT_FOUND).entity(e).build();
			}
			
			String apiKey = ResourceService.getApiKey(headers);
			if (apiKey == null || !u.checkApiKey(apiKey)) {
				String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4021); 
				return Response.status(Status.UNAUTHORIZED).entity(e).build();
			}
			
			Endpoint e = EndpointService.getUserEndpoint(u, endpointId);
			if (e == null) {
				String e2 = JsonMessageManager.jsonMessage(404, "endpoint not found", 4021); 
				return Response.status(Status.NOT_FOUND).entity(e2).build();
			}
			endpointDao.delete(e);

			String json = JsonMessageManager.jsonMessage(200, "endpoint deleted", 2020);
			
			return Response.ok().entity(json).build();
		} catch (SQLException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5020); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}

}
