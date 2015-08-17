import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import fileauth.FileAuthScanJob;
import helpers.JobHandler;
import jobs.CheckJob;
import jobs.FolderSizesJob;
import jobs.ImportJob;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import services.JobService;

public class Global extends GlobalSettings {
	private List<JobHandler> jobHandlers = new ArrayList<JobHandler>();
	public void onStart(Application app) {
		outputTools();
		JobService.addJob(new FileAuthScanJob());
		JobService.addJob(new ImportJob());
		JobService.addJob(new CheckJob());	
		JobService.addJob(new FolderSizesJob());	
	}

	public void onStop(Application app) {
		for(JobHandler handler : jobHandlers) {
			handler.stop();
		}
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
