package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
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

import org.glassfish.jersey.process.internal.RequestScoped;

import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.EmailManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.TemplateHTML;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.EndpointService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;

/**
 * Groups all tested endpoint management related endpoints. Create/delete endpoints
 * Error codes: X1XX
 */
@Path("/ci")
@RequestScoped
public class ContinuousIntegrationResource {

    private static final Logger LOGGER = Logger.getLogger(ContinuousIntegrationResource.class.getName());


    /**
     * Register new endpoint
     *
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
        	// Check if the record exists in the database already.
            Endpoint e = EndpointService.getEndpointWithEmailAndUrlAndFreq(endp.getEmail(), endp.getUrl(), endp.getFrequency());
            if (e != null && e.isConfirmed()) {
                String e2 = JsonMessageManager.jsonMessage(400, "Url already in use", 4100);
                return Response.status(Status.BAD_REQUEST).entity(e2).build();
            } else if (e != null && !e.isConfirmed()) {
                EmailManager em = new EmailManager(endp);
                em.sendConfirmation();
                String e2 = JsonMessageManager.jsonMessage(200, "We'll resend you a confirmation email.", 2100);
                return Response.status(Status.ACCEPTED).entity(e2).build();
            } else {
                endpointDao.create(endp);

                EmailManager em = new EmailManager(endp);
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
     * Confirm an email address. We assume if they have the Id then they got the email.
     *
     * @param endpointId Endpoint id
     * @return json message
     */
    @GET
    @Path("/confirmation")
    @Produces(MediaType.TEXT_HTML)
    public Response confirm(@QueryParam("key") String endpointId) {

        LOGGER.log(Level.FINER, "New GET /ci/confirm call. EndpointId: " + endpointId);

        try {
            TemplateHTML result;
            Boolean confirmed = EndpointService.confirmEndpointWithId(endpointId);
            if (confirmed == null) {
                String e2 = JsonMessageManager.jsonMessage(404, "endpoint not found", 4101);
                return Response.status(Status.NOT_FOUND).entity(e2).build();
            } else if (confirmed) {
                result = new TemplateHTML("/templates/confirmation.html");
            } else {
                result = new TemplateHTML("/templates/already-confirmed.html");
            }

            return Response.ok().entity(result.generateBody()).build();
        } catch (SQLException | IOException | URISyntaxException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5101);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

    /**
     * Delete an endpoint. We assume if they have the Id then they can delete it (for now).
     *
     * @param endpointId Endpoint id
     * @return json message
     */
    @GET
    @Path("/unsubscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response deleteEndpoint(@QueryParam("key") String endpointId,
                                   @Context HttpHeaders headers) {

        LOGGER.log(Level.FINER, "New DELETE /ci/unsubscribe call. EndpointId: " + endpointId);

        try {
            TemplateHTML result;
            Boolean unsubscribed = EndpointService.deleteEndpointWithId(endpointId);
            if (!unsubscribed) {
                String e2 = JsonMessageManager.jsonMessage(404, "endpoint not found", 4102);
                return Response.status(Status.NOT_FOUND).entity(e2).build();
            } else {
                result = new TemplateHTML("/templates/unsubscribe.html");
            }

            return Response.ok().entity(result.generateBody()).build();
        } catch (SQLException | IOException | URISyntaxException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5102);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
}
