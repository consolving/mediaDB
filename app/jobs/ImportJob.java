package jobs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.typesafe.config.ConfigFactory;
import java.nio.file.Files;
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
			return !arg0.isDirectory() && !name.startsWith(".");
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
		String storageFolder = ROOT_DIR + File.separator + "storage";
		if (mediaFolder.exists()) {
			for (File f : mediaFolder.listFiles(FILE_NAME_FILTER)) {
				if (count < 0) {
					return;
				}
				checksum = OpensslHelper.getSha256Checksum(f);
				MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
				if (checksum == null) {
					Logger.error("cannot create checksum for " + f.getAbsolutePath());
				} else if (mf == null) {
					mf = new MediaFile();
					mf.checksum = checksum;
					mf.filename = f.getName();
					mf = addMimeType(f, mf);
					mf.save();
					mf = MediaFileHelper.probeFile(mf, f);
					mf.save();
					Logger.info("created " + f.getAbsolutePath() + " Checksum " + checksum);
					handleFile(f, new File(storageFolder + File.separator + checksum));
					count--;
				} else {
					Logger.info(f.getAbsolutePath() + " already found! Checksum " + checksum);
					try {
						FileUtils.forceDelete(f);
						Logger.info("deleting " + f.getAbsolutePath());
					} catch (IOException ioe) {
						Logger.error("Problem operating on filesystem, deleting " + f.getAbsolutePath());
					}
				}
				if (f != null && mf != null) {
					MediaFileHelper.addTags(mf, f.getName());				
					mf.save();
				}
			}
		}
	}

	private MediaFile addMimeType(File f, MediaFile mf) {
		mf.mimeType = MediaFileHelper.getFileMimeType(f);
		if(mf.mimeType == null) {
			try {
				mf.mimeType = Files.probeContentType(f.toPath());
			} catch (IOException e) {
				Logger.error("Problem operating on filesystem, finding MimeType of " + f.getAbsolutePath());
			}						
		}
		return mf;
	}

	private void handleFile(File from, File to) {
		if (from.exists()) {
			try {
				FileUtils.moveFile(from, to);
				Logger.info("moving " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
			} catch (IOException ioe) {
				Logger.error("Problem operating on filesystem, moving " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
			}
		}
	}
}
