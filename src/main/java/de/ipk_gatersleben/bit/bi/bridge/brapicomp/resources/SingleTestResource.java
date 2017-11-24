package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import static de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ResourceService.findTestCollection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
 * Request a single test or testcollection against an endpoint.
 * Error code: X2XX
 */
@Path("/test")
@RequestScoped
public class SingleTestResource {

    private static final Logger LOGGER = LogManager.getLogger(SingleTestResource.class.getName());

    /**
     * Run a structure test for one endpoint
     *
     * @param url     Url of the BrAPI server. Example: https://test.brapi.org/brapi/v1/
     * @param name    Name of the test to run. Must match the name of the test in respources/test.json
     * @param uriInfo Contains the rest of query parameters.
     * @return Response with json report
     */
    @GET
    @Path("/structure")
    @Produces(MediaType.APPLICATION_JSON)
    public Response singleSchemaTest(@QueryParam("url") String url, @QueryParam("name") String name, @Context UriInfo uriInfo) {

        LOGGER.debug("New GET /structure call.");
        try {

            if (url.equals("")) {
                String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4200);
                return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
            }

            ObjectMapper mapper = new ObjectMapper();
            String json;

            
            if (name.equals("all")) {
            	// All non-parametric tests.
            	
                String collectionResource = "/collections/SimpleStructureTest.custom_collection.json";

                InputStream inJson = TestCollection.class.getResourceAsStream(collectionResource);
                TestCollection tc = mapper.readValue(inJson, TestCollection.class);

                Endpoint endp = new Endpoint(url);
                TestSuiteReport testSuiteReport = RunnerService.testEndpoint(endp, tc);
                json = mapper.writeValueAsString(testSuiteReport);

                
            } else {
            	// Single test
            	
                Test test = new Test(name);

                VariableStorage storage = new VariableStorage();

                MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
                int params = 0;
                
                // Store and clean parameters.
                for (String key : queryParams.keySet()) {
                    if (!(key.equals("name") || key.equals("url"))) {
                        key = key.replaceAll("[^A-Za-z0-9_]", ""); //Clean string
                        String value = queryParams.getFirst(key);
                        value = value.replaceAll("[^A-Za-z0-9_]", "");
                        storage.setVariable(key, "\"" + value + "\"");
                        params++;
                    }

                }
                TestItemReport testSuiteReport;

                Item simple;

                if (params > 0) {
                    test.setEndpoint(RunnerService.replaceVariablesUrl(test.getEndpoint(), storage));
                    simple = new Item("GET", url, test);
                    testSuiteReport = RunnerService.singleTestEndpoint(simple, storage);
                } else {
                    simple = new Item("GET", url, test);
                    testSuiteReport = RunnerService.singleTestEndpoint(simple);
                }
                json = mapper.writeValueAsString(testSuiteReport);
            }

            return Response.ok().entity(json).build();
        } catch (IOException e) {
            //Thrown by .getResourceAsStream(""). Most probably because of missing file or wrong config structure.
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5200);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4201);
            return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
        }
    }

    /**
     * Run a data test for one endpoint
     *
     * @param url  Url of the BrAPI server. Example: https://test.brapi.org/brapi/v1/
     * @param name Name of the test to run. Must match the name of the test in respources/test.json
     * @return Response with json report
     */
    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response dataTest(@QueryParam("url") String url, @QueryParam("name") String name) {

        LOGGER.debug("New GET /data call.");
        try {

            if (url.equals("")) {
                String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4202);
                return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
            }

            ObjectMapper mapper = new ObjectMapper();

            String collectionResource;

            collectionResource = "/collections" + findTestCollection(name);

            InputStream inJson = TestCollection.class.getResourceAsStream(collectionResource);
            TestCollection tc = mapper.readValue(inJson, TestCollection.class);

            Endpoint endp = new Endpoint(url);
            TestSuiteReport testSuiteReport = RunnerService.testEndpoint(endp, tc);

            return Response.ok().entity(mapper.writeValueAsString(testSuiteReport)).build();
        } catch (IOException e) {
            //Thrown by .getResourceAsStream(""). Most probably because of missing file or wrong config structure.
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5201);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
}
