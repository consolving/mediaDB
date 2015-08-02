package jobs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.typesafe.config.ConfigFactory;

import controllers.Jobs;
import helpers.MediaFileHelper;
import helpers.OpensslHelper;
import models.MediaFile;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

public class ImportJob implements Runnable {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return !pathname.isDirectory() && !pathname.getName().startsWith(".");
		}
	};

	public static void schedule() {
		ImportJob job = new ImportJob();
		Akka.system().scheduler().schedule(Duration.create(200, TimeUnit.MILLISECONDS),
				Duration.create(getValue("job.importjob.runEvery", 10), TimeUnit.MINUTES), // run job every 10 minutes
				job, Akka.system().dispatcher());
	}

	@Override
	public void run() {
		if(Jobs.isJobActive("ImportJob")) {
			Logger.info("Running ImportJob.");
			Jobs.setLastRun("ImportJob");
			importNext();
		} else {
			Logger.info("ImportJob not active. Skipping.");
		}
	}

	private void importNext() {
		int count = getValue("job.importjob.numerOfImports", 25);
		String checksum;
		File mediaFolder = new File(ROOT_DIR + File.separator + "upload");
		String storageFolder = ROOT_DIR + File.separator + "storage";
		if (mediaFolder.exists()) {
			for (File f : mediaFolder.listFiles(FILE_FILTER)) {
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
					mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
					handleFile(f, new File(storageFolder + File.separator + checksum));
					count--;
				} else {
					Logger.info(f.getAbsolutePath() + " already found! Checksum " + checksum);
					handleFile(f, new File(storageFolder + File.separator + checksum));
				}
				if (f != null && mf != null && f.getName().equals(mf.checksum)) {
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
			Logger.info("moving " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
		}
	}
	
	private static int getValue(String key, int defaultValue) {
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
