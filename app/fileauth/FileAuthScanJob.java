package fileauth;

import jobs.AbstractJob;
import services.JobService;

/**
 * ScanJob Periodically Scan of user/group files.
 * 
 * @author Philipp Haussleiter
 * 
 */

public class FileAuthScanJob extends AbstractJob {
	
	public FileAuthScanJob() {
		super("FileAuthScanJob");
		cancellable = false;
	}
	
	@Override
	public void run() {
		JobService.setLastRun(getName());
		FileAuth.scanUsers();
		FileAuth.scanGroups();
	}
	
}
