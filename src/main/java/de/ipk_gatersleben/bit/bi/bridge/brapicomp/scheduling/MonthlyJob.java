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

public class MonthlyJob implements org.quartz.Job {
	private static final Logger LOGGER = Logger.getLogger(MonthlyJob.class.getName());
	
    public MonthlyJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
    	LOGGER.fine("Monthly is executing.");
        ObjectMapper mapper = new ObjectMapper();

        InputStream inJson = TestCollection.class.getResourceAsStream("/collections/CompleteBrapiTest.custom_collection.json");
        TestCollection tc;
		try {
			tc = mapper.readValue(inJson, TestCollection.class);
			RunnerService.TestAllEndpointsWithFreq(tc, "monthly");
		} catch (IOException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
}
