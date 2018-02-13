package de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.MiniTestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReportService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ApiResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.CSVUtils;
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReport(@PathParam("reportId") String reportId) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_IMPLEMENTED).build();
        }
    	LOGGER.debug("New GET /testreport call. Id: " + reportId);
        try {
            TestReport tr = TestReportService.getReport(reportId);
            if (tr == null) {
                String e2 = JsonMessageManager.jsonMessage(404, "testReport not found", 4300);
                return Response.status(Status.NOT_FOUND).entity(e2).build();
            }
            return Response.ok().entity(tr).build();

        } catch (SQLException e) {
            //Thrown by TestReportService.getReport(reportId)
            LOGGER.warn("SQLException while calling GET /reportId with id: " + reportId + ". " + e.getMessage());
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5300);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
    
    /**
     * Get reduced report in a specific format
     *
     * @return JSON with endpoint data
     */
    @GET
    @Path("/shortreport/{reportId}/{format}")
    @Produces({MediaType.APPLICATION_JSON, "text/csv"})
    public Response resourcesReport(@PathParam("reportId") String reportId, @PathParam("format") String format) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_IMPLEMENTED).build();
        }
        LOGGER.debug("New GET /shortreport/" + format + ". Id: " + reportId);
        
        if (!format.equalsIgnoreCase("csv") && !format.equalsIgnoreCase("json")) {
        	String e1 = JsonMessageManager.jsonMessage(400, "Invalid format.", 4400);
            return Response.status(Status.BAD_REQUEST).entity(e1).build();
        }
        
        try {
        	TestReport tr = TestReportService.getReport(reportId);
        	
        	String output;
        	if (format.equalsIgnoreCase("csv")) {
        		output = generateCSVReport(tr);
        	} else {
        		output = generateJSONReport(tr);
        	}
        	
        	return Response.ok().entity(output).build();
        	
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5401);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

	private String generateCSVReport(TestReport tr) throws JsonProcessingException, IOException {
		String csv = "";
		MiniTestReport miniReport = tr.getMiniReport();
		// Columns
		String resourceName = tr.getResource().getName();
		String crop = tr.getResource().getCrop();
		String baseURL = tr.getResourceUrl();
		String warningCalls = miniReport.getWarningTests().toString();
		String failedCalls = miniReport.getFailedTests().toString();
		String testedCalls = miniReport.getTotalTests().toString();
		String warningCallsCount = ""+miniReport.getWarningTests().size();
		String failedCallsCount = ""+miniReport.getFailedTests().size();
		String testedCallsCount = ""+miniReport.getTotalTests().size();
		String medianTestTime = Double.toString(miniReport.getTime());
		String linkFullTest = Config.get("baseDomain") + "?report=" + tr.getReportId().toString();
		
		List<String> header = Arrays.asList("resourceName", 
				"crop", "baseURL", "warningCalls", 
				"failedCalls", "testedCalls","warningCallsCount", 
				"failedCallsCount", "testedCallsCount", "medianTestTimeMS", "linkFullTest");
		csv += CSVUtils.writeLine(header, '\t');
		
		List<String> values = Arrays.asList(resourceName,
				crop, baseURL, warningCalls, 
				failedCalls, testedCalls, warningCallsCount, failedCallsCount, testedCallsCount, medianTestTime, linkFullTest);		
		csv += CSVUtils.writeLine(values, '\t');
		
		return csv;
	}

	private String generateJSONReport(TestReport tr) throws IOException {
		Map<String, Object> json = new HashMap<String, Object>();
		MiniTestReport miniReport = tr.getMiniReport();
		
		json.put("resourceName", tr.getResource().getName());
		json.put("crop", tr.getResource().getCrop());
		json.put("baseURL", tr.getResourceUrl());
		json.put("implementedCalls", miniReport.getTotalTests());
		json.put("successfulCalls", miniReport.getPassedTests());
		json.put("failedCalls", miniReport.getFailedTests());
		json.put("medianTestTimeMS", miniReport.getTime());
		json.put("linkFullTest", Config.get("baseDomain") + "?report=" + tr.getReportId().toString());
		
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(json);
	}

}
