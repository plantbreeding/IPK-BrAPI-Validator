package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;

/**
 * Sends transactional emails.
 * Runs a test on an endpoint and sends an email to the owner.
 */
public class EmailManager {
    private static final Logger LOGGER = Logger.getLogger(EmailManager.class.getName());
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
            LOGGER.warning("Problem sending confirmation for endpoint: " + this.endpoint.getId().toString() + ". Error: " + e.getMessage());
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
    public boolean sendReport(TestSuiteReport testReport) {
        String body;
        try {
            Map<String, String> variables = getReportTemplateVariables(testReport);
            TemplateHTML email = new TemplateHTML("/templates/email.html", variables);
            body = email.generateBody();
            EmailSender.sendEmail(body, "BrAPI Validator report", endpoint.getEmail());
            return true;
        } catch (IOException | URISyntaxException e) {
            LOGGER.warning("Problem sending report for endpoint: " + this.endpoint.getId().toString() + ". Error: " + e.getMessage());
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
        return map;
    }

	public List<TestReport> getPrevReports() {
		return prevReports;
	}

	public void setPrevReports(List<TestReport> prevReports) {
		this.prevReports = prevReports;
	}
}
