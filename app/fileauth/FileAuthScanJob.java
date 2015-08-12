package fileauth;

import java.util.concurrent.TimeUnit;

import akka.actor.Cancellable;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

/**
 * ScanJob Periodically Scan of user/group files.
 * 
 * @author Philipp Haussleiter
 * 
 */

public class FileAuthScanJob implements Runnable {
	private static Cancellable Instance = null;
	@Override
	public void run() {
		FileAuth.scanUsers();
		FileAuth.scanGroups();
	}

	public static void schedule() {
		FileAuthScanJob job = new FileAuthScanJob();
		Instance = Akka.system()
				.scheduler()
				.schedule(Duration.create(200, TimeUnit.MILLISECONDS),
						Duration.create(5, TimeUnit.MINUTES),  // run job every 1 minutes
						job, Akka.system().dispatcher());
		Logger.info("FileAuthScanJob is scheduled!");
	}
	
	public static void cancel() {
		if(Instance != null) {
			Instance.cancel();
			Logger.info("FileAuthScanJob was canceled!");
		}
	}
}
