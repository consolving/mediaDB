import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import fileauth.FileAuthScanJob;
import helpers.JobHandler;
import jobs.FileCheckJob;
import jobs.FolderSizesJob;
import jobs.ImportJob;
import models.MediaFolder;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import services.JobService;

public class Global extends GlobalSettings {
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private List<JobHandler> jobHandlers = new ArrayList<JobHandler>();
	public void onStart(Application app) {
		MediaFolder.getOrCreate(File.separator);
		outputTools();
		checkFolders();
		if(app.isDev()) {
			Logger.info("#### INIT DEV ENV ####");
			new DevelopmentEnvironment(app).init();
		}
		JobService.addJob(new FileAuthScanJob());
		JobService.addJob(new ImportJob());
		JobService.addJob(new FileCheckJob());	
		JobService.addJob(new FolderSizesJob());	
		Logger.info("#### APPLICATION STARTED ####");
	}

	public void onStop(Application app) {
		for(JobHandler handler : jobHandlers) {
			handler.stop();
		}
		Logger.info("#### APPLICATION STOPPED ####");
	}
	
	private void outputTools() {
		String[] keys = {	"media.ffmpeg.bin",
							"media.ffprobe.bin",
							"system.openssl.bin",
							"system.bash.bin",
							"system.file.bin",
							"system.du.bin",
							"system.mv.bin",
							"system.rm.bin",
							"system.cp.bin"};
		for(String key : keys){
			if(new File(ConfigFactory.load().getString(key)).exists()) {
				Logger.info(key+" => "+ConfigFactory.load().getString(key)+" exists!");
			} else {
				Logger.warn(key+" => "+ConfigFactory.load().getString(key)+" missing!");
			}
		}
	}
	
	private void checkFolders() {
		String[] folders = {
				ROOT_DIR+File.separator + "upload",
				ROOT_DIR+File.separator + "trash",
				ROOT_DIR+File.separator + "storage",
				ROOT_DIR+File.separator + "tmp",
				ROOT_DIR+File.separator + "thumbnails"
		};
		for(String folder : folders){
			if(!new File(folder).exists()) {
				Logger.info("creating " + folder);
				new File(folder).mkdirs();
			}
		}
	}
}
