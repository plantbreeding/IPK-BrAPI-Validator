package de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class WeeklyJob implements org.quartz.Job {
    public WeeklyJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.err.println("Hello World!  MyJob is executing.");
    }
}
