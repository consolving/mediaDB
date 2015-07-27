package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.Configuration;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class Jobs extends Controller {
	public final static String[] JOBS = { "ImportJob" };
	private final static SimpleDateFormat JOBS_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Result toggleJobActive(String jobname) {
		String key1 = "job." + jobname + ".active";
		String key2 = "jobs.active";
		if (isJobActive(jobname)) {
			Configuration.set(key1, "false");
		} else {
			Configuration.set(key1, "true");
			Configuration.set(key2, "true");
		}
		Configuration.set("jobs.stats", null);
		return redirect(routes.Application.index());
	}

	public static Result toggleJobsActive() {
		if (isJobsActive()) {
			for (String job : JOBS) {
				Configuration.set("job." + job + ".active", "false");	
				Configuration.set("jobs.active", "false");
			}
		} else {
			for (String job : JOBS) {
				Configuration.set("job." + job + ".active", "true");	
				Configuration.set("jobs.active", "true");
			}			
		}
		Configuration.set("jobs.stats", null);
		return redirect(routes.Application.index());	
	}
	
	public static boolean isJobActive(String jobname) {
		String key = "job." + jobname + ".active";
		String value = Configuration.get(key) != null ? Configuration.get(key) : "false";
		return Boolean.parseBoolean(value);
	}

	public static boolean isJobsActive() {
		String key = "jobs.active";
		String value = Configuration.get(key) != null ? Configuration.get(key) : "false";
		return Boolean.parseBoolean(value);
	}
	
	public static String getJobsStats() {
		String jobstats = Configuration.get("jobs.stats");
		if (jobstats != null) {
			return jobstats;
		}
		int active = 0;
		for (String job : JOBS) {
			active += isJobActive(job) ? 1 : 0;
		}
		jobstats = active + "/" + JOBS.length;
		Configuration.set("jobs.stats", jobstats);
		return jobstats;
	}
	
	public static void setLastRun(String jobname) {
		String key = "job." + jobname + ".last";
		Configuration.set(key, JOBS_DATE.format(new Date()));
	}
	
	public static Date getLastRun(String jobname) {
		String key = "job." + jobname + ".last";
		try {
			return Configuration.get(key) != null ? JOBS_DATE.parse(Configuration.get(key)) : new Date();
		} catch (ParseException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
			return new Date();
		}
	}
}
