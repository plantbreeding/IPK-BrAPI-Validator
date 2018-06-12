package de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ApiResourceService;
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
     * Run a call test
     *
     * @param url  Url of the BrAPI server. Example: https://test.brapi.org/brapi/v1/
     * @return Response with json report
     */
    @GET
    @Path("/call")
    @Produces(MediaType.APPLICATION_JSON)
    public Response callTest(@QueryParam("url") String url, 
    		@QueryParam("brapiversion") @DefaultValue("") String version,
    		@QueryParam("strict") @DefaultValue("") String strict) {

        LOGGER.debug("New GET /call call.");
        try {
            if (url.equals("")) {
                String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid url parameter", 4202);
                return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
            }
            if (!version.equals("v1.0") && !version.equals("v1.1") && !version.equals("v1.2")) {
                String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid version parameter", 4202);
                return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
            }
            
            String collectionResource;
            
            ObjectMapper mapper = new ObjectMapper();

            boolean allowAdditional = !strict.equals("on");
            collectionResource = "/collections/CompleteBrapiTest." + version + ".json";

            InputStream inJson = TestCollection.class.getResourceAsStream(collectionResource);
            TestCollection tc = mapper.readValue(inJson, TestCollection.class);
            
            Resource res = new Resource(url);
            TestSuiteReport testSuiteReport = RunnerService.testEndpointWithCall(res, tc, allowAdditional);
            TestReport report = new TestReport(res, mapper.writeValueAsString(testSuiteReport));
            
            
            return Response.ok().entity(report).build();
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(400, "invalid URL: port must be 80", 5201);
            return Response.status(Status.BAD_REQUEST).entity(e1).build();
        } catch (IOException e) {
            //Thrown by .getResourceAsStream(""). Most probably because of missing file or wrong config structure.
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5201);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
}
