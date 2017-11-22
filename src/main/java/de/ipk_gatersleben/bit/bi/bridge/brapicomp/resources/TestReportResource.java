package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.EmailManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.TemplateHTML;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.EndpointService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReportService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TestReport related endpoints
 * Error code: X3XX
 */
@Path("/")
@RequestScoped
public class TestReportResource {

    private static final Logger LOGGER = Logger.getLogger(TestReportResource.class.getName());

    @GET
    @Path("/testReport/{reportId}")
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public Response getReport(@PathParam("reportId") String reportId) {

        try {
            TestReport tr = TestReportService.getReport(reportId);
            if (tr == null) {
                //String e2 = JsonMessageManager.jsonMessage(404, "testReport not found", 4300);
                return Response.status(Status.NOT_FOUND).build(); //Test this
            }

            Map<String, String> attVariables = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            attVariables.put("report", mapper.writeValueAsString(tr.getReportJson()));
            TemplateHTML attachmentHTML = new TemplateHTML("/templates/report.html", attVariables);
            return Response.ok().entity(attachmentHTML.generateBody()).build();

        } catch (SQLException e) {
            //Thrown by TestReportService.getReport(reportId)
            LOGGER.warning("SQLException while calling GET /reportId with id: " + reportId + ". " + e.getMessage());
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5300);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        } catch (IOException | URISyntaxException e) {
            //Thrown by new TemplateHTML("/templates/report.html", attVariables)
            LOGGER.warning("Exception while generating report: " + reportId + ". " + e.getMessage());
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5301);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

    @GET
    @Path("/testReport/{reportId}/json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportJson(@PathParam("reportId") String reportId) {

        try {
            TestReport tr = TestReportService.getReport(reportId);
            if (tr == null) {
                String e2 = JsonMessageManager.jsonMessage(404, "testReport not found", 4300);
                return Response.status(Status.NOT_FOUND).entity(e2).build(); //Test this
            }

            return Response.ok().entity(tr.getReportJson()).build();


        } catch (SQLException e) {
            //Thrown by TestReportService.getReport(reportId)
            LOGGER.warning("SQLException while calling GET /reportId with id: " + reportId + ". " + e.getMessage());
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5300);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

}
