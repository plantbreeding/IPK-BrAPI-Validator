package de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

public class WeeklyJob implements org.quartz.Job {
	private static final Logger LOGGER = LogManager.getLogger(WeeklyJob.class.getName());
    public WeeklyJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
       LOGGER.info("Weekly is executing.");
       ObjectMapper mapper = new ObjectMapper();

       InputStream inJson = TestCollection.class.getResourceAsStream("/collections/CompleteBrapiTest." + Config.get("scheduledVersion") + ".json");
       TestCollection tc;
		try {
			tc = mapper.readValue(inJson, TestCollection.class);
			RunnerService.TestAllEndpointsWithFreq(tc, "weekly");
			RunnerService.TestAllPublicEndpoints(tc);
		} catch (IOException | SQLException e1) {
			e1.printStackTrace();
		}
    }
}
