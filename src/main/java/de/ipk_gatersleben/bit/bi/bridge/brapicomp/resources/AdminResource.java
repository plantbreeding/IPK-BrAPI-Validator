package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
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
 * Error codes: X0XX
 */
@Path("/admin")
@RequestScoped
public class AdminResource {

    private static final Logger LOGGER = Logger.getLogger(AdminResource.class.getName());

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
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4000);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }

            //Check if api key is correct.
            if (!auth[1].equals(Config.get("adminkey"))) {
                String e = JsonMessageManager.jsonMessage(403, "missing or wrong apikey", 4001);
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
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5001);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

    /*@DELETE
    @Path("/tables")
    public Response delTables() {
        try {
            DataSourceManager.deleteTable(TestReport.class);
            DataSourceManager.deleteTable(Endpoint.class);
            DataSourceManager.createTable(TestReport.class);
            DataSourceManager.createTable(Endpoint.class);
        } catch (SQLException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().build();

        //DataSourceManager.deleteTable();
    }*/
}
