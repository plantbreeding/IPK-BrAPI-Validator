package de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling;

import org.quartz.Scheduler;

public class SchedulerManager {
	private static Scheduler scheduler;

	public static Scheduler getScheduler() {
		return scheduler;
	}

	public static void setScheduler(Scheduler scheduler) {
		SchedulerManager.scheduler = scheduler;
	}
}
