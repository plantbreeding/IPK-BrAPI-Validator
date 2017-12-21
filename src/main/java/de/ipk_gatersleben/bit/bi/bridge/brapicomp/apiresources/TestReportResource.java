package de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.TemplateHTML;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.ResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReportService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;

/**
 * TestReport related endpoints
 * Error code: X3XX
 */
@Path("/")
@RequestScoped
public class TestReportResource {

    private static final Logger LOGGER = LogManager.getLogger(TestReportResource.class.getName());

    @GET
    @Path("/testreport/{reportId}")
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public Response getReport(@PathParam("reportId") String reportId) {

    	LOGGER.debug("New GET /testreport call. Id: " + reportId);
        try {
            TestReport tr = TestReportService.getReport(reportId);
            if (tr == null) {
                String e2 = JsonMessageManager.jsonMessage(404, "testReport not found", 4300);
                return Response.status(Status.NOT_FOUND).build(); //Test this
            }

            Map<String, String> attVariables = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            attVariables.put("report", mapper.writeValueAsString(tr.getReportJson()));
            attVariables.put("timestamp", tr.getDate().toString());
            
            
            Resource endp = tr.getEndpoint();
            Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
            
            endpointDao.refresh(endp);
            
            attVariables.put("email", endp.getEmail());
            TemplateHTML attachmentHTML = new TemplateHTML("/templates/report.html", attVariables);
            return Response.ok().entity(attachmentHTML.generateBody()).build();

        } catch (SQLException e) {
            //Thrown by TestReportService.getReport(reportId)
            LOGGER.warn("SQLException while calling GET /reportId with id: " + reportId + ". " + e.getMessage());
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5300);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        } catch (IOException | URISyntaxException e) {
            //Thrown by new TemplateHTML("/templates/report.html", attVariables)
            LOGGER.warn("Exception while generating report: " + reportId + ". " + e.getMessage());
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5301);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
    
    @GET
    @Path("/lastreport/{resourceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastReport(@PathParam("resourceId") String resourceId) {

    	LOGGER.debug("New GET /lastreport call. Id: " + resourceId);
        try {
        	Resource e = ResourceService.getPublicEndpoint(resourceId);
        	if (e == null) {
                String e2 = JsonMessageManager.jsonMessage(404, "resource not found", 4301);
                return Response.status(Status.NOT_FOUND).build();
            }
            List<TestReport> trl = TestReportService.getLastReports(e, 1);
            if (trl.size() == 0 || trl.get(0) == null) {
                String e2 = JsonMessageManager.jsonMessage(404, "testReport not found", 4302);
                return Response.status(Status.NOT_FOUND).build();
            }

            return Response.ok().entity(trl.get(0)).build();

        } catch (SQLException e) {
            //Thrown by TestReportService.getReport(reportId)
            LOGGER.warn("SQLException while calling GET /lastreport/{resourceId} with id: " + resourceId + ". " + e.getMessage());
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5302);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
}
