package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.resources.ContinuousIntegration;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestCollectionRunner;

/**
 * Sends transactional emails.
 * Runs a test on an endpoint and sends an email to the owner.
 *
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
		} catch (IOException | URISyntaxException e){
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
	 * @return True if no errors occurred.
	 */
	public boolean runAndSend(TestCollection testCollection) {
		TestCollectionReport ter = new TestCollectionRunner(testCollection, endpoint.getUrl()).runTests();
		String body;
		try {
			Map<String, String> variables = getReportTemplateVariables(ter);
			TemplateHTML email = new TemplateHTML("/templates/email.html", variables);
			body = email.generateBody();
			EmailSender.sendEmail(body, "BrAPI Validator report", endpoint.getEmail());
			return true;
		} catch (IOException | URISyntaxException e) {
			LOGGER.warning("Problem sending report for endpoint: " + this.endpoint.getId().toString() + ". Error: " + e.getMessage());
		}
		return false;
	}

	private Map<String, String> getReportTemplateVariables(TestCollectionReport ter) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("frequency", endpoint.getFrequency());
		map.put("url", endpoint.getUrl());
		map.put("contactEmail", Config.get("contactEmail"));
		map.put("senderName", Config.get("senderName"));
		map.put("baseDomain", Config.get("baseDomain"));
		map.put("endpointId", endpoint.getId().toString());
		map.put("testName", ter.getName());
		map.put("timestamp","TODO");
		map.put("passed", "0");
		map.put("failed", "30");
		map.put("total", "30");
		map.put("failing list", "List\nList");
		
		return map;
	}	
}
