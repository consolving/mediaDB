package helpers;

import java.util.concurrent.TimeUnit;

import akka.actor.Cancellable;
import jobs.AbstractJob;
import models.Configuration;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import services.JobService;

public class JobHandler {
	
	private AbstractJob job = null;
	private Cancellable jobCancellable = null;

	public JobHandler() {
		
	}
	
	public JobHandler(AbstractJob job) {
		this.job = job;
	}

	public void schedule() {
		if(job != null && job.getName() != null) {
			jobCancellable = Akka.system().scheduler().schedule(Duration.create(30, TimeUnit.SECONDS),
					Duration.create(job.getRunEvery(), TimeUnit.MINUTES), 
					job, Akka.system().dispatcher());
			Configuration.set("job." + job.getName() + ".active", "true");
			Configuration.set("jobs.stats", null);	
			JobService.setStatus(job.getName(), JobService.Status.STARTED);
		}
	}

	public void run() {
		if(job != null && job.getName() != null) {
			job.run();
		}
	}
	
	public void stop() {
		if(jobCancellable != null) {
			Logger.info(job.getName()+" not active. Stopping.");	
			Configuration.set("job." + job.getName() + ".active", "false");
			Configuration.set("jobs.stats", null);	
			jobCancellable.cancel();
			JobService.setStatus(job.getName(), JobService.Status.STOPPED);
		}
	}

	public AbstractJob getJob() {
		return job;
	}
	
	public boolean isCancellable() {
		return job.isCancellable();
	}
}
