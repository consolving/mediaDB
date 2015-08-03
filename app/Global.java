import java.io.File;

import com.typesafe.config.ConfigFactory;

import jobs.ImportJob;
import play.Application;
import play.GlobalSettings;
import play.Logger;

public class Global extends GlobalSettings {
	
	public void onStart(Application app) {
		outputTools();
		ImportJob.schedule();
	}

	public void onStop(Application app) {
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
