package jobs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.TimeUnit;

import com.typesafe.config.ConfigFactory;

import helpers.MediaFileHelper;
import helpers.OpensslHelper;
import models.MediaFile;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

public class ImportJob implements Runnable {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");

	private final static FilenameFilter FILE_NAME_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File arg0, String name) {
			return !name.startsWith(".");
		}

	};

	public static void schedule() {
		ImportJob job = new ImportJob();
		Akka.system().scheduler().schedule(Duration.create(200, TimeUnit.MILLISECONDS),
				Duration.create(10, TimeUnit.MINUTES), // run job every 10
														// minutes
				job, Akka.system().dispatcher());
	}

	@Override
	public void run() {
		importNext();
	}

	private void importNext() {
		int count = 10;
		String checksum;
		File mediaFolder = new File(ROOT_DIR + File.separator + "upload");
		if (mediaFolder.exists()) {
			for (File f : mediaFolder.listFiles(FILE_NAME_FILTER)) {
				if (count < 0) {
					return;
				}
				checksum = OpensslHelper.getSha256Checksum(f);
				MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
				if(checksum == null) {
					Logger.error("cannot create checksum for "+f.getAbsolutePath());
				} else if ( mf == null) {
					mf = new MediaFile();
					mf.checksum = checksum;
					mf.filename = f.getName();
					mf.save();
					mf = MediaFileHelper.probeFile(mf, f);
					mf.save();
					Logger.debug("created " + f.getAbsolutePath() + " Checksum " + checksum);
				} else {
					Logger.debug(f.getAbsolutePath() + " already found! Checksum " + checksum);
				}
				if(f != null && mf != null) {
					MediaFileHelper.addTags(mf, f.getName());
					mf.save();
				}
				count--;
			}
		}
	}
}
