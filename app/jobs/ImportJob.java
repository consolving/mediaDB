package jobs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.typesafe.config.ConfigFactory;

import helpers.MediaFileHelper;
import helpers.OpensslHelper;
import models.MediaFile;
import play.Logger;
import services.JobService;

public class ImportJob extends AbstractJob {

	public ImportJob() {
		super("ImportJob");
	}

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static File MEDIA_FOLDER = new File(ROOT_DIR + File.separator + "upload");
	private final static String STORAGE_FOLDER = ROOT_DIR + File.separator + "storage";
	private final static FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return !file.getName().startsWith(".");
		}
	};

	@Override
	public void run() {
		if(JobService.isJobActive(getName())) {
			JobService.setLastRun(getName());
			importNext();
		} else if(jobHandler != null){		
			jobHandler.stop();
		}	
	}

	private List<File> scanFolder(File folder, List<File> files, int count) {
		for(File f : folder.listFiles(FILE_FILTER)){
			if (count < 0) {
				return files;
			}
			if(f.isDirectory()) {
				files = scanFolder(f, files, count);
			} else {
				files.add(f);
			}
			count--;
			Logger.debug(f.getAbsolutePath()+"!="+MEDIA_FOLDER.getAbsolutePath()+": "+ MediaFileHelper.getCount(f));
			if(MediaFileHelper.getCount(f) == 0 && !f.getAbsolutePath().equals(MEDIA_FOLDER.getAbsolutePath())) {
				MediaFileHelper.delete(f.getAbsolutePath().endsWith("/") ? f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-1) : f.getAbsolutePath());
			}
		}
		return files;
	}
	
	private void importNext() {
		int count = getValue("job.importjob.numerOfImports", 25);
		String checksum;
		if (MEDIA_FOLDER.exists()) {
			for (File f : scanFolder(MEDIA_FOLDER, new ArrayList<File>(), count)) {
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
					handleFile(f, new File(STORAGE_FOLDER + File.separator + checksum));
				} else {
					Logger.info(f.getAbsolutePath() + " already found! Checksum " + checksum);
					handleFile(f, new File(STORAGE_FOLDER + File.separator + checksum));
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
		if(!to.exists()) {
			try {	
				FileUtils.moveFile(from, to);
				Logger.info("moving " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
				
			} catch (IOException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		} else {
			FileUtils.deleteQuietly(from);
			Logger.info("deleting" + from.getAbsolutePath() + " already a copy present!");	
		}
	}
}
