package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;

/**
 * Sends transactional emails.
 * Runs a test on an endpoint and sends an email to the owner.
 */
public class EmailManager {
    private static final Logger LOGGER = LogManager.getLogger(EmailManager.class.getName());
    private Endpoint endpoint;
    private List<TestReport> prevReports;

    public EmailManager(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public boolean sendConfirmation() {
        String body;
        try {
            Map<String, String> variables = getConfirmationTemplateVariables();
            TemplateHTML email = new TemplateHTML("/templates/email-confirmation.html", variables);
            body = email.generateBody();
            EmailSender.sendEmail(body, "BrAVA: Confirm your email", endpoint.getEmail());
            return true;
        } catch (IOException | URISyntaxException e) {
            LOGGER.info("Problem sending confirmation for endpoint: " + this.endpoint.getId().toString() + ". Error: " + e.getMessage());
        }
        return false;
    }


    private Map<String, String> getConfirmationTemplateVariables() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("frequency", endpoint.getFrequency());
        map.put("url", endpoint.getUrl());
        map.put("contactEmail", Config.get("contactEmail"));
        map.put("senderName", Config.get("senderName"));
        map.put("endpointId", endpoint.getId().toString());
        map.put("baseDomain", Config.get("baseDomain"));
        return map;
    }

    /**
     * Run the tests in testCollection and send an email with the report.
     * Returns true if no errors occurred and the email was sent
     *
     * @return True if no errors occurred.
     */
    public boolean sendReport(TestSuiteReport testSuiteReport) {
        String body;
        try {
            Map<String, String> variables = getReportTemplateVariables(testSuiteReport);
            TemplateHTML email = new TemplateHTML("/templates/email.html", variables);
            body = email.generateBody();
            
            // Subject variables.
            String url = endpoint.getUrl();
            String passed = Integer.toString(testSuiteReport.getTestCollections().get(0).getTotal()
                    - testSuiteReport.getTestCollections().get(0).getFails());
            String total = Integer.toString(testSuiteReport.getTestCollections().get(0).getTotal());
            
            
            String subject = "[BrAVa - Report for scheduled BrAPI test] Passed tests: " + passed + "/" + total + " for " + url;
            EmailSender.sendEmail(body, subject, endpoint.getEmail());
            return true;
        } catch (IOException | URISyntaxException e) {
            LOGGER.info("Problem sending report for endpoint: " + this.endpoint.getId().toString() + ". Error: " + e.getMessage());
        }
        return false;
    }

    private Map<String, String> getReportTemplateVariables(TestSuiteReport tsr) {
        tsr.getTestCollections().get(0).addStats();
        Map<String, String> map = new HashMap<>();
        map.put("frequency", endpoint.getFrequency());
        map.put("url", endpoint.getUrl());
        map.put("contactEmail", Config.get("contactEmail"));
        map.put("senderName", Config.get("senderName"));
        map.put("baseDomain", Config.get("baseDomain"));
        map.put("endpointId", endpoint.getId().toString());
        map.put("testName", tsr.getTestCollections().get(0).getName());
        map.put("passed", Integer.toString(tsr.getTestCollections().get(0).getTotal()
                - tsr.getTestCollections().get(0).getFails()));
        map.put("total", Integer.toString(tsr.getTestCollections().get(0).getTotal()));
        map.put("failList", tsr.getTestCollections().get(0).getFailListAsHTML());
        map.put("reportId", tsr.getId());
        map.put("prevReports", getPrevReportsHTML());
        return map;
    }

    /**
     * Generate the HTML for the PrevReports section in the email.
     * @return
     */
    private String getPrevReportsHTML() {
    	String prevReportsHTML = "";
    	if (prevReports.size() > 0) {
    		prevReportsHTML += "<p>Older reports:</p>\n";
    		prevReportsHTML += "<ul>\n";
    		for (int i = 0; i < prevReports.size(); i++) {
    			String url = Config.get("baseDomain") + "/api/testreport/" + prevReports.get(i).getReportId().toString();
    			prevReportsHTML += "<li><a href=\"" + url + "\">" + url + "</a> - " 
    			+ prevReports.get(i).getDate().toString() + "</li>\n";
    		}
    		prevReportsHTML += "</ul>\n";
    	}
    	return prevReportsHTML;
    }
    
	public List<TestReport> getPrevReports() {
		return prevReports;
	}

	public void setPrevReports(List<TestReport> prevReports) {
		this.prevReports = prevReports;
	}
}
