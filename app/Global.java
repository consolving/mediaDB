import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import fileauth.FileAuthScanJob;
import jobs.ImportJob;
import jobs.JobHandler;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import services.JobService;

public class Global extends GlobalSettings {
	private List<JobHandler> jobHandlers = new ArrayList<JobHandler>();
	public void onStart(Application app) {
		FileAuthScanJob.schedule();
		outputTools();
		JobService.addJob(new ImportJob());
	}

	public void onStop(Application app) {
		for(JobHandler handler : jobHandlers) {
			handler.stop();
		}
		FileAuthScanJob.cancel();
	}
	
	private void outputTools() {
		String[] keys = {	"media.ffmpeg.bin",
							"media.ffprobe.bin",
							"system.openssl.bin",
							"system.bash.bin",
							"system.file.bin",
							"system.du.bin",
							"system.mv.bin"};
		for(String key : keys){
			if(new File(ConfigFactory.load().getString(key)).exists()) {
				Logger.info(key+" => "+ConfigFactory.load().getString(key)+" exists!");
			} else {
				Logger.warn(key+" => "+ConfigFactory.load().getString(key)+" missing!");
			}
		}
	}
}
