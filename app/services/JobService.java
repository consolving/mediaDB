package services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import helpers.JobHandler;
import jobs.AbstractJob;
import jobs.NopJob;
import models.Configuration;
import play.Logger;

public class JobService {
	private JobService() {

	}
	private final static SimpleDateFormat JOBS_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static AbstractJob NOP_JOB = new NopJob();
	private final static JobHandler NOP_HANDLER = new JobHandler(NOP_JOB);
	private static Map<String, JobHandler> jobHandlers = new HashMap<>();
	
	public static enum Status {
		STARTED("started"),
		RUNNING("running"), 
		STOPPING("stopping"),
		STOPPED("stopped");
		private final String statusValue;

		private Status(final String status) {
			statusValue = status;
		}

		public String toString() {
			return statusValue;
		}
	};
	public static void addJob(AbstractJob job) {
		JobHandler handler = new JobHandler(job);
		handler.schedule();
		jobHandlers.put(job.getName(), handler);
	}
	
	public static JobHandler getHandler(String jobname) {
		if(jobHandlers.containsKey(jobname)){
			return jobHandlers.get(jobname);
		}
		return NOP_HANDLER;
	}
	
	public static boolean isCancellable(String jobname) {
		if(jobHandlers.containsKey(jobname)){
			return jobHandlers.get(jobname).isCancellable();
		}
		return false;
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
		for (String job : getJobNames()) {
			active += isJobActive(job) ? 1 : 0;
		}
		jobstats = active + "/" + getJobNames().size();
		Configuration.set("jobs.stats", jobstats);
		return jobstats;
	}

	public static void setLastRun(String jobname) {
		Logger.info("Running "+jobname);
		String key = "job." + jobname + ".last";
		Configuration.set(key, JOBS_DATE.format(new Date()));
		setStatus(jobname, Status.RUNNING);
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
	
	public static Date getNextRun(String jobname) {
		String key1 = "job." + jobname + ".last";
		String key2 = "job." + jobname + ".runEvery";
		try {
			Date last = Configuration.get(key1) != null ? JOBS_DATE.parse(Configuration.get(key1)) : new Date();
			Integer every = Integer.parseInt(Configuration.get(key2) != null ? Configuration.get(key2): "5");
			DateTime dt = new DateTime(last);
			return dt.plusMinutes(every).toDate();
		} catch(NumberFormatException | ParseException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
			return new Date();
		}		
	}
	
	public static void toggleJobActive(String jobname) {
		String key1 = "job." + jobname + ".active";
		String key2 = "jobs.active";
		if(!isCancellable(jobname)) {
			return;
		}
		if (JobService.isJobActive(jobname)) {
			Configuration.set(key1, "false");
			JobService.setStatus(jobname, JobService.Status.STOPPING);
		} else {
			Configuration.set(key1, "true");
			Configuration.set(key2, "true");
			JobService.getHandler(jobname).schedule();
		}
		Configuration.set("jobs.stats", null);		
	}
	
	public static void toggleJobsActive() {
		if (JobService.isJobsActive()) {
			for (String job : JobService.getJobNames()) {
				if(isCancellable(job)) {
					Configuration.set("job." + job + ".active", "false");
					Configuration.set("jobs.active", "false");
					JobService.setStatus(job, JobService.Status.STOPPING);					
				}	
			}
		} else {
			for (String job : JobService.getJobNames()) {
				if(isCancellable(job)) {
					Configuration.set("job." + job + ".active", "true");
					Configuration.set("jobs.active", "true");
					JobService.setStatus(job, JobService.Status.STARTED);
				}
			}
		}
		Configuration.set("jobs.stats", null);		
	}
	
	public static Set<String> getJobNames() {
		return jobHandlers.keySet();
	}
	
	public static AbstractJob getJob(String jobname) {
		return getHandler(jobname).getJob();
	}
	
	public static void setStatus(String jobname, Status status) {	
		String key = "job." + jobname + ".status";
		Configuration.set(key, status.toString());		
	}
	
	public static String getStatus(String jobname) {
		String key = "job." + jobname + ".status";
		return Configuration.get(key);		
	}
}
