package de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MonthlyJob implements org.quartz.Job {
    public MonthlyJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.err.println("Hello World!  MyJob is executing.");
    }
}
