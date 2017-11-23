package de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.TemplateHTML;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReportService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;

/**
 * TestReport related endpoints
 * Error code: X3XX
 */
@Path("/")
@RequestScoped
public class TestReportResource {

    private static final Logger LOGGER = Logger.getLogger(TestReportResource.class.getName());

    @GET
    @Path("/testreport/{reportId}")
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
            attVariables.put("timestamp", tr.getDate().toString());
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
}
