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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ResourceService;
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
     *
     * @return Response with json report
     */
    @POST
    @Path("/testall")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generalTest(@Context HttpHeaders headers, @QueryParam("frequency") String frequency) {

        LOGGER.log(Level.FINER, "New POST /testall call.");
        try {

            String[] auth = ResourceService.getAuth(headers);
            //Check auth header
            if (auth == null || auth.length != 2) {
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4021);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }

            //Check if api key is correct.
            if (!auth[1].equals(Config.get("adminkey"))) {
                String e = JsonMessageManager.jsonMessage(403, "missing or wrong apikey", 4010);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }


            ObjectMapper mapper = new ObjectMapper();

            InputStream inJson = TestCollection.class.getResourceAsStream("/collections/CompleteBrapiTest.custom_collection.json");
            TestCollection tc = mapper.readValue(inJson, TestCollection.class);

            int count = RunnerService.TestAllEndpointsWithFreq(tc, frequency);
            boolean success = false;
            if (count > 0) {
                success = true;
            }
            return Response.status(Status.ACCEPTED).entity(success).build();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5010);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

    /**
     * Run the default test over all the email's endpoints
     *
     * @param email Email who's endpoints will be tested
     * @return Response with json report
     */
    @POST
    @Path("/usertest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response userTest(@Context HttpHeaders headers, @QueryParam("email") String email) {

        LOGGER.log(Level.FINER, "New POST /usertest call.");
        try {
            String[] auth = ResourceService.getAuth(headers);
            //Check auth header
            if (auth == null || auth.length != 2) {
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4021);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }


            //Check if api key is correct.
            //Disabled
            if (true || auth[0] != Config.get("admin") || auth[1] != Config.get("key")) {
                String e = JsonMessageManager.jsonMessage(403, "missing or wrong apikey", 4010);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }

            ObjectMapper mapper = new ObjectMapper();

            InputStream inJson = TestCollection.class.getResourceAsStream("/BrapiTesting.custom_collection.json");
            TestCollection tc = mapper.readValue(inJson, TestCollection.class);

            List<TestSuiteReport> testSuiteList = RunnerService.testAllEmailEndpoints(email, tc);

            return Response.status(Status.ACCEPTED).entity(mapper.writeValueAsString(testSuiteList)).build();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5010);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
}
