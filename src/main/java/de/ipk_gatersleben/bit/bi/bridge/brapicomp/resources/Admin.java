package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.User;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.UserService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

/**
 * Currently groups the Administrator related queries.
 * Mostly running tests on endpoints stored in the database.
 */
@Path("/admin")
@RequestScoped
public class Admin {

	private static final Logger LOGGER = Logger.getLogger(Admin.class.getName());
	
	/**
	 * Run the default test on all endpoints
	 * @param apikey API key is required for all admin calls.
	 * @return Response with json report
	 */
	@POST
	@Path("/generaltest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generalTest(@QueryParam("apikey") String apikey) {

		LOGGER.log(Level.FINER, "New POST /generaltest call.");
		try {
			if (apikey == null || !apikey.equals(Config.get("adminkey"))) {
				String e = JsonMessageManager.jsonMessage(403, "missing or wrong apikey", 4010); 
				return Response.status(Status.UNAUTHORIZED).entity(e).build();
			}
			ObjectMapper mapper = new ObjectMapper();
			
			InputStream inJson = TestCollection.class.getResourceAsStream("/BrapiTesting.custom_collection.json");
			TestCollection tc = mapper.readValue(inJson, TestCollection.class);
			
			List<TestSuiteReport> testSuiteList = RunnerService.TestAllEndpoints(tc);
			
			return Response.status(Status.ACCEPTED).entity(mapper.writeValueAsString(testSuiteList)).build();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5010); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}
	/**
	 * Run the default test over all the user's endpoints
	 * @param apikey API key is required for all admin calls.
	 * @param user Username of the user who's endpoints will be tested
	 * @return Response with json report
	 */
	@POST
	@Path("/usertest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response userTest(@QueryParam("apikey") String apikey, @QueryParam("username") String user) {

		LOGGER.log(Level.FINER, "New POST /usertest call.");
		try {
			if (apikey == null || !apikey.equals(Config.get("adminkey"))) {
				String e = JsonMessageManager.jsonMessage(403, "missing or wrong apikey", 4010); 
				return Response.status(Status.UNAUTHORIZED).entity(e).build();
			}
			User u = UserService.getUser(user);
			if (u == null) {
				String e = JsonMessageManager.jsonMessage(404, "user not found", 4020); 
				return Response.status(Status.NOT_FOUND).entity(e).build();
			}
			ObjectMapper mapper = new ObjectMapper();
			
			InputStream inJson = TestCollection.class.getResourceAsStream("/BrapiTesting.custom_collection.json");
			TestCollection tc = mapper.readValue(inJson, TestCollection.class);
			
			List<TestSuiteReport> testSuiteList = RunnerService.testAllUserEndpoints(u, tc);
			
			return Response.status(Status.ACCEPTED).entity(mapper.writeValueAsString(testSuiteList)).build();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5010); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}
}
