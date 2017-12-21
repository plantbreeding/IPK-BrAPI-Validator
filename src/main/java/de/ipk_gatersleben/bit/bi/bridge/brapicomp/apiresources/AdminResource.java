package de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.EmailManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.ResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReportService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ApiResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

/**
 * Currently groups the Administrator related queries.
 * Mostly running tests on endpoints stored in the database.
 * Error codes: X0XX
 */
@Path("/admin")
@RequestScoped
public class AdminResource {

    private static final Logger LOGGER = LogManager.getLogger(AdminResource.class.getName());

    //Commented out for security. Also private API key is required
    /*
    *//**
     * Run the default test on all endpoints
     *
     * @return Response with json report
     *//*
    @POST
    @Path("/testall")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generalTest(@Context HttpHeaders headers, @QueryParam("frequency") String frequency) {

        LOGGER.debug("New POST /testall call.");
        try {

            String[] auth = ApiResourceService.getAuth(headers);
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

    @DELETE
    @Path("/tables")
    public Response delTables(@Context HttpHeaders headers) {
    	// It is necessary to delete or update the database tables after the schema changes
    	// i.e. when a new parameter/column is added
        try {
        	
        	String[] auth = ApiResourceService.getAuth(headers);
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
            
            DataSourceManager.deleteTable(TestReport.class);
            DataSourceManager.deleteTable(Resource.class);
        } catch (SQLException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }
    @GET
    @Path("/users")
    public Response getUsers(@Context HttpHeaders headers) {
    	// It is necessary to delete or update the database tables after the schema changes
    	// i.e. when a new parameter/column is added
    	List<Resource> l;
        try {
        	
        	String[] auth = ApiResourceService.getAuth(headers);
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
        	
            l = ResourceService.getAllEndpoints();
            
        } catch (SQLException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().entity(l.toString()).build();
    }*/
    
    /**
     * Register new public endpoint
     *
     * @param res Json containing the endpoint's url.
     * @return Json message.
     */
    @POST
    @Path("/resources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEndpoint(@Context HttpHeaders headers,
                                   Resource res) {

        LOGGER.debug("New POST /admin/resources call.");
        
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);

        try {

        	res.setEmail(null);
        	res.setPublic(true);
            endpointDao.create(res);
            return Response.status(Status.ACCEPTED).entity(JsonMessageManager.jsonMessage(200, "Public endpoint added.", 2101)).build();
        
        } catch (SQLException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5002);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
    //Commented out for security. Also private API key is required
    
    /**
     * Run the default test on all public resources
     *
     * @return Response with json report
     */
    @GET
    @Path("/testallpublic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generalTest(@Context HttpHeaders headers) {

        LOGGER.debug("New POST /testallpublic call.");
        try {

            String[] auth = ApiResourceService.getAuth(headers);
            //Check auth header
            if (auth == null || auth.length != 2) {
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4002);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }

            //Check if api key is correct.
            if (!auth[1].equals(Config.get("adminkey"))) {
                String e = JsonMessageManager.jsonMessage(403, "missing or wrong apikey", 4003);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }
            
            ObjectMapper mapper = new ObjectMapper();

            InputStream inJson = TestCollection.class.getResourceAsStream("/collections/CompleteBrapiTest.custom_collection.json");
            TestCollection tc = mapper.readValue(inJson, TestCollection.class);

            List<Resource> publicResources = ResourceService.getAllPublicEndpoints();
            
            publicResources.forEach(resource -> {
            	try {
					RunnerService.TestEndpointWithCallAndSaveReport(resource, tc);
				} catch (SQLException | JsonProcessingException e) {
					e.printStackTrace();
				} 
            });
            return Response.ok().build();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5003);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
}
