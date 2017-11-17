package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources.ContinuousIntegration;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestCollectionRunner;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

/**
 * Sends transactional emails.
 * Runs a test on an endpoint and sends an email to the owner.
 */
public class EmailManager {
    private static final Logger LOGGER = Logger.getLogger(EmailManager.class.getName());
    private Endpoint endpoint;

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
    public boolean runAndSend(TestCollection testCollection) {
        TestSuiteReport testSuiteReport = RunnerService.testEndpoint(endpoint, testCollection);
        String body;
        try {
            Map<String, String> variables = getReportTemplateVariables(testSuiteReport);
            TemplateHTML email = new TemplateHTML("/templates/email.html", variables);
            body = email.generateBody();

            Map<String, String> attVariables = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            attVariables.put("report", mapper.writeValueAsString(testSuiteReport));
            TemplateHTML attachmentHTML = new TemplateHTML("/templates/report.html", attVariables);

            Attachment attachment = new Attachment("report.html", attachmentHTML.generateBody());
            EmailSender.sendEmail(body, "BrAPI Validator report", endpoint.getEmail(), attachment);
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
        map.put("failed", Integer.toString(tsr.getTestCollections().get(0).getFails()));
        map.put("total", Integer.toString(tsr.getTestCollections().get(0).getTotal()));
        map.put("failList", tsr.getTestCollections().get(0).getFailListAsHTML());
        return map;
    }
}
