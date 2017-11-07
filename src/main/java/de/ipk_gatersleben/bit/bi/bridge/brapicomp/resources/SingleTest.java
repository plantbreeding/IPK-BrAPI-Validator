package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import static de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ResourceService.findTestCollection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.Test;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestItemReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

/**
 * Request a single test or testcollection against an endpoint
 *
 */
@Path("/test")
@RequestScoped
public class SingleTest {
	
	private static final Logger LOGGER = Logger.getLogger(SingleTest.class.getName());
	/**
	 * Run the default test for one endpoint
	 * @param url Url of the BrAPI server. Example: https://test.brapi.org/brapi/v1/
	 * @param name Name of the test to run. Must match the name of the test in respources/test.json
	 * @return Response with json report
	 */
	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fullSchemaTest(@QueryParam("url") String url, @QueryParam("name") String name) {

		LOGGER.log(Level.FINER, "New POST /all call.");
		try {

			if (url.equals("") || url == null) {
				String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4030);
				return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
			}
			
			
			ObjectMapper mapper = new ObjectMapper();
			
			String collectionResource = null;

			collectionResource = "/collections/SimpleStructureTest.custom_collection.json";
			
			InputStream inJson = TestCollection.class.getResourceAsStream(collectionResource);
			TestCollection tc = mapper.readValue(inJson, TestCollection.class);
			
			Endpoint endp = new Endpoint(url);
			TestSuiteReport testSuiteReport = RunnerService.testEndpoint(endp, tc);
			
			return Response.status(Status.ACCEPTED).entity(mapper.writeValueAsString(testSuiteReport)).build();
		} catch (IOException e) {
			//Thrown by .getResourceAsStream(""). Most probably because of missing file or wrong config structure.
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5030); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}
	/**
	 * Run the default test for one endpoint
	 * @param url Url of the BrAPI server. Example: https://test.brapi.org/brapi/v1/
	 * @param name Name of the test to run. Must match the name of the test in respources/test.json
	 * @param uriInfo Contains the rest of query parameters.
	 * @return Response with json report
	 */
	@GET
	@Path("/single")
	@Produces(MediaType.APPLICATION_JSON)
	public Response singleSchemaTest(@QueryParam("url") String url, @QueryParam("name") String name, @Context UriInfo uriInfo) {
		
		LOGGER.log(Level.FINER, "New POST /single call.");
		try {

			if (url.equals("") || url == null) {
				String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4030);
				return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
			}
			
			ObjectMapper mapper = new ObjectMapper();
			
			Test test = new Test(name);
			
			VariableStorage storage = new VariableStorage();
			
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			int keys = 0;
			for (String key : queryParams.keySet()) {
				if (!(key.equals("name") || key.equals("url"))) {
					key = key.replaceAll("[^A-Za-z0-9_]",""); //Clean string
					String value = queryParams.getFirst(key);
					value = value.replaceAll("[^A-Za-z0-9_]","");
					storage.setVariable(key, "\"" + value + "\"");
					keys++;
				}
				
			}
			TestItemReport testSuiteReport = null;
			
			Item simple;
			
			if (keys > 0) {
				test.setEndpoint(RunnerService.replaceVariablesUrl(test.getEndpoint(), storage));
				simple = new Item("GET", url, test);
				testSuiteReport = RunnerService.singleTestEndpoint(simple, storage);
			} else {
				simple = new Item("GET", url, test);
				testSuiteReport = RunnerService.singleTestEndpoint(simple);
			}

			return Response.status(Status.ACCEPTED).entity(mapper.writeValueAsString(testSuiteReport)).build();
		} catch (IOException e) {
			//Thrown by .getResourceAsStream(""). Most probably because of missing file or wrong config structure.
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5030); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4031);
			return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
		}
	}
	
	/**
	 * Run the default test for one endpoint
	 * @param url Url of the BrAPI server. Example: https://test.brapi.org/brapi/v1/
	 * @param name Name of the test to run. Must match the name of the test in respources/test.json
	 * @return Response with json report
	 */
	@GET
	@Path("/data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dataTest(@QueryParam("url") String url, @QueryParam("name") String name) {

		LOGGER.log(Level.FINER, "New POST /data call.");
		try {

			if (url.equals("") || url == null) {
				String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4030);
				return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
			}
			
			
			ObjectMapper mapper = new ObjectMapper();
			
			String collectionResource;

			collectionResource = "/collections" + findTestCollection(name);
			
			InputStream inJson = TestCollection.class.getResourceAsStream(collectionResource);
			TestCollection tc = mapper.readValue(inJson, TestCollection.class);
			
			Endpoint endp = new Endpoint(url);
			TestSuiteReport testSuiteReport = RunnerService.testEndpoint(endp, tc);
			
			return Response.status(Status.ACCEPTED).entity(mapper.writeValueAsString(testSuiteReport)).build();
		} catch (IOException e) {
			//Thrown by .getResourceAsStream(""). Most probably because of missing file or wrong config structure.
			e.printStackTrace();
			String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5030); 
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
		}
	}
}
