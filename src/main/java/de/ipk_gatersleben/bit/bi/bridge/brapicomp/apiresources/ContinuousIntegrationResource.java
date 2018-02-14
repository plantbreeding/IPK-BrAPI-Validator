package de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.EmailManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.TemplateHTML;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.ResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;

/**
 * Groups all tested endpoint management related endpoints. Create/delete endpoints
 * Error codes: X1XX
 */
@Path("/ci")
@RequestScoped
public class ContinuousIntegrationResource {

    private static final Logger LOGGER = LogManager.getLogger(ContinuousIntegrationResource.class.getName());


    /**
     * Register new resource
     *
     * @param endp Json containing the resource's url.
     * @return Json message.
     */
    @POST
    @Path("/resources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEndpoint(@Context HttpHeaders headers,
                                   Resource res) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOGGER.debug("New POST /ci/resources call.");
        
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);

        try {
        	// Check if the record exists in the database already.
            Resource e = ResourceService.getEndpointWithEmailAndUrlAndFreq(res.getEmail(), res.getUrl(), res.getFrequency());
            if (res.getEmail() == null) {
            	String e2 = JsonMessageManager.jsonMessage(400, "Invalid email", 4100);
                return Response.status(Status.BAD_REQUEST).entity(e2).build();
            } else if (e != null && e.isConfirmed()) {
                String e2 = JsonMessageManager.jsonMessage(400, "Url already in use", 4101);
                return Response.status(Status.BAD_REQUEST).entity(e2).build();
            } else if (e != null && !e.isConfirmed()) {
                EmailManager em = new EmailManager(res);
                em.sendConfirmation();
                String e2 = JsonMessageManager.jsonMessage(200, "We'll resend you a confirmation email.", 2100);
                return Response.status(Status.ACCEPTED).entity(e2).build();
            } else {
            	res.setPublic(false);
                endpointDao.create(res);

                EmailManager em = new EmailManager(res);
                em.sendConfirmation();

                return Response.status(Status.ACCEPTED).entity(JsonMessageManager.jsonMessage(200, "We'll send you a confirmation email.", 2101)).build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5100);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
    
    /**
     * Change an resource frequency
     *
     * @param res Json containing the res' url.
     * @return Json message.
     */
    @GET //This is called by a link in an email, so we only have GET
    @Path("/resources/{resourceId}/frequency")
    @Produces(MediaType.TEXT_HTML)
    public Response changeFrequency(@Context HttpHeaders headers,
                                   @PathParam("resId") String resId, @QueryParam("frequency") String frequency) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOGGER.debug("New GET /ci/resources/{resourceId}/frequency call. Id: " + resId);
        
        try {
        	TemplateHTML result;
        	// Check if the record exists in the database already.
            Boolean changed = ResourceService.changeEndpointFreqWithId(resId, frequency);
            if (changed == null) {
            	String e2 = JsonMessageManager.jsonMessage(404, "endpoint not found", 4102);
                return Response.status(Status.NOT_FOUND).entity(e2).build();
            } else if (changed) {
            	result = new TemplateHTML("/templates/freq-updated.html");  
            	return Response.ok().entity(result.generateBody()).build();
            } else {
            	String e2 = JsonMessageManager.jsonMessage(400, "Invalid frequency", 4103);
                return Response.status(Status.BAD_REQUEST).entity(e2).build();
            }
        } catch (IOException | SQLException | URISyntaxException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5101);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

    /**
     * Confirm an email address. We assume if they have the Id then they got the email.
     *
     * @param resId Resource id
     * @return json message
     */
    @GET
    @Path("/confirmation")
    @Produces(MediaType.TEXT_HTML)
    public Response confirm(@QueryParam("key") String resId) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOGGER.debug("New GET /ci/confirm call. EndpointId: " + resId);

        try {
            TemplateHTML result;
            Boolean confirmed = ResourceService.confirmEndpointWithId(resId);
            if (confirmed == null) {
                String e2 = JsonMessageManager.jsonMessage(404, "endpoint not found", 4104);
                return Response.status(Status.NOT_FOUND).entity(e2).build();
            } else if (confirmed) {
                result = new TemplateHTML("/templates/confirmation.html");
            } else {
                result = new TemplateHTML("/templates/already-confirmed.html");
            }

            return Response.ok().entity(result.generateBody()).build();
        } catch (SQLException | IOException | URISyntaxException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5102);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

    /**
     * Delete an endpoint. We assume if they have the Id then they can delete it (for now).
     *
     * @param resId Resource id
     * @return json message
     */
    @GET
    @Path("/unsubscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response deleteEndpoint(@QueryParam("key") String resId,
                                   @Context HttpHeaders headers) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOGGER.debug("New DELETE /ci/unsubscribe call. EndpointId: " + resId);

        try {
            TemplateHTML result;
            Boolean unsubscribed = ResourceService.deleteEndpointWithId(resId);
            if (!unsubscribed) {
                String e2 = JsonMessageManager.jsonMessage(404, "endpoint not found", 4105);
                return Response.status(Status.NOT_FOUND).entity(e2).build();
            } else {
                result = new TemplateHTML("/templates/unsubscribe.html");
            }

            return Response.ok().entity(result.generateBody()).build();
        } catch (SQLException | IOException | URISyntaxException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5103);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
}
