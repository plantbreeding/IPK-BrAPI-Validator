package de.ipk_gatersleben.bit.bi.bridge.brapicomp;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.*;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Provider;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Stat;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling.DailyJob;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling.MonthlyJob;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling.SchedulerManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.scheduling.WeeklyJob;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import io.restassured.RestAssured;

/**
 * Initializes tables, Daos and others.
 */
@WebListener
public class AppServletContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			DataSourceManager.closeConnectionSource();
			SchedulerManager.getScheduler().shutdown();

		} catch (IOException | SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent e) {
		Config.init();
		createDatabaseConnection(e.getServletContext());
		createTables();
		buildDaos();
		setupScheduler(e.getServletContext());
		if (Config.get("proxy") != null) {
			RestAssured.proxy(Config.get("proxy"), Integer.parseInt(Config.get("proxyport")));
		}

	}

	private static void createDatabaseConnection(ServletContext servletContext) {
		String path = Config.get("dbpath");
		String databaseUrl = path;
		ConnectionSource connectionSource;
		try {
			connectionSource = new JdbcPooledConnectionSource(databaseUrl);
			((JdbcPooledConnectionSource) connectionSource).setUsername(Config.get("dbuser"));
			((JdbcPooledConnectionSource) connectionSource).setPassword(Config.get("dbpass"));
			if (connectionSource.getDatabaseType().getClass()
					.equals(com.j256.ormlite.db.OracleDatabaseType.class)) {
				Cache.addToCache("dbType", "oracle");
			} else {
				Cache.addToCache("dbType", "oracle");
			}
			;
			DataSourceManager.setConnectionSource(connectionSource);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void createTables() {
		try {
			DataSourceManager.createTable(Resource.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			DataSourceManager.createTable(Provider.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			DataSourceManager.createTable(TestReport.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			DataSourceManager.createTable(Stat.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void buildDaos() {
		try {
			DataSourceManager.addDao(Resource.class);
			DataSourceManager.addDao(Provider.class);
			DataSourceManager.addDao(TestReport.class);
			DataSourceManager.addDao(Stat.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void setupScheduler(ServletContext ctx) {
		String key = "org.quartz.impl.StdSchedulerFactory.KEY";
		StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getAttribute(key);
		try {
			Scheduler quartzScheduler = factory.getScheduler("QuartzSchedulerInstance");

			JobDetail dailyJob = newJob(DailyJob.class).withIdentity("dailyJob", "group1").build();

			JobDetail weeklyJob = newJob(WeeklyJob.class).withIdentity("weeklyJob", "group1").build();

			JobDetail monthlyJob = newJob(MonthlyJob.class).withIdentity("monthlyJob", "group1").build();

			Trigger dailyTrigger = newTrigger().withIdentity("daily", "group1").startNow()
					//.withSchedule(simpleSchedule().withIntervalInMinutes(3).repeatForever()) // debug
					.withSchedule(dailyAtHourAndMinute(8, 0)) // fire every day at 08:00
					.build();

			Trigger weeklyTrigger = newTrigger().withIdentity("weekly", "group1").startNow()
					//.withSchedule(simpleSchedule().withIntervalInMinutes(4).repeatForever()) //
					// .withSchedule(dailyAtHourAndMinute(15, 20)) // debug
					.withSchedule(weeklyOnDayAndHourAndMinute(DateBuilder.MONDAY, 8, 0)) // fire every Monday at 08:00
					.build();

			Trigger monthlyTrigger = newTrigger().withIdentity("monthly", "group1").startNow()
					//.withSchedule(simpleSchedule().withIntervalInMinutes(5).repeatForever()) // debug
					.withSchedule(monthlyOnDayAndHourAndMinute(1, 8, 0)) // fire every 1st at 08:00
					.build();
			
			quartzScheduler.scheduleJob(dailyJob, dailyTrigger);
			quartzScheduler.scheduleJob(weeklyJob, weeklyTrigger);
			quartzScheduler.scheduleJob(monthlyJob, monthlyTrigger);

			quartzScheduler.start();

			SchedulerManager.setScheduler(quartzScheduler);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
