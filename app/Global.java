import jobs.ImportJob;
import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {
	
	public void onStart(Application app) {
		ImportJob.schedule();
	}

	public void onStop(Application app) {
	}
}
