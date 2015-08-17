package jobs;

import com.typesafe.config.ConfigFactory;

import helpers.JobHandler;
import models.Configuration;
import play.Logger;

public abstract class AbstractJob  implements Runnable {
	private String name;
	protected JobHandler jobHandler = null;
	protected boolean cancellable = true;
	public AbstractJob(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getRunEvery() {
		Configuration.set("job."+getName()+".runEvery", String.valueOf(getValue("job."+getName()+".runEvery", 10)));
		return getValue("job."+getName()+".runEvery", 10);
	}
	
	public void setJobHandler(JobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}
	
	public boolean isCancellable() {
		return this.cancellable;
	}
	
	protected static int getValue(String key, int defaultValue) {
		int value = defaultValue;
		if(ConfigFactory.load().hasPath(key)){
			try {
				value = Integer.parseInt(ConfigFactory.load().getString(key));
			} catch(NumberFormatException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		}
		return value;
	}
	
}
