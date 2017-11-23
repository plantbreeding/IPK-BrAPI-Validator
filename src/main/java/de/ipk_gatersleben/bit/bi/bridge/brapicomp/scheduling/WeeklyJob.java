package de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

public class WeeklyJob implements org.quartz.Job {
	private static final Logger LOGGER = Logger.getLogger(WeeklyJob.class.getName());
    public WeeklyJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
       LOGGER.fine("Weekly is executing.");
       ObjectMapper mapper = new ObjectMapper();

       InputStream inJson = TestCollection.class.getResourceAsStream("/collections/CompleteBrapiTest.custom_collection.json");
       TestCollection tc;
		try {
			tc = mapper.readValue(inJson, TestCollection.class);
			RunnerService.TestAllEndpointsWithFreq(tc, "weekly");
		} catch (IOException | SQLException e1) {
			e1.printStackTrace();
		}
    }
}
