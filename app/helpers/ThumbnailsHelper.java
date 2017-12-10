package helpers;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.persistence.OptimisticLockException;

import org.apache.commons.io.FileUtils;

import com.typesafe.config.ConfigFactory;

import models.MediaFile;
import models.Thumbnail;
import play.Logger;

public class ThumbnailsHelper {
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String THUMBNAILS_DIR = ROOT_DIR + File.separator + "thumbnails";
	private final static String STORAGE_FOLDER = ROOT_DIR + File.separator + "storage";
	
	private ThumbnailsHelper() {

	}

	public static File createThumbnail(MediaFile mediaFile, String size) {
		Logger.info("creating Thumbnail in " + size + " for " + mediaFile.filepath);
		if (mediaFile == null || size == null || !size.contains("x")) {
			return null;
		}
		if (mediaFile.mimeType.startsWith("image")) {
			return createImageThumbnail(mediaFile, size);
		}
		if (mediaFile.mimeType.startsWith("video")) {
			return createVideoThumbnail(mediaFile, 1, size);
		}
		return null;
	}

	public static File createImageThumbnail(MediaFile mediaFile, String size) {
		checkDir(THUMBNAILS_DIR + File.separator + SystemHelper.getFoldersForName(mediaFile.checksum));
		String thumbPath = SystemHelper.getFolders(mediaFile.checksum) + File.separator + "thumb_0_" + size + ".png";
		File file = new File(STORAGE_FOLDER + File.separator + mediaFile.getLocation());
		File thumb = new File(THUMBNAILS_DIR + File.separator + thumbPath);
		Logger.info("creating " + thumb.getAbsolutePath() + " from " + file.getAbsolutePath());
		if (!thumb.exists() || mediaFile.cover == null) {

			File oldThumb = new File(THUMBNAILS_DIR + File.separator + mediaFile.checksum + "thumb_0_" + size + ".png");

			if (file.exists() && oldThumb.exists()) {
				checkThumbnailLocation(mediaFile, thumb, oldThumb);
			} else if (file.exists()) {
				try {
					BufferedImage img = ImageIO.read(file);
					Size s = getSize(size);
					BufferedImage scaled = scale(img, s.w, s.h);
					ImageIO.write(scaled, "png", thumb);
					String checksum = ThumbnailsHelper.getETag(thumb);
					Thumbnail.getOrCreate(mediaFile, thumbPath, checksum);
					if (mediaFile.cover == null) {
						mediaFile.cover = Thumbnail.getOrCreate(mediaFile, thumbPath, checksum);
						mediaFile.update();
					}
				} catch (IOException ex) {
					Logger.warn(ex.getLocalizedMessage(), ex);
				} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
					Logger.warn(ex.getLocalizedMessage(), ex);
				} catch( OptimisticLockException ex ) {
					Logger.warn(ex.getLocalizedMessage(), ex);					
				}

			} else {
				Logger.warn(file.getAbsolutePath() + " does not exists!");
			}
		}
		return thumb;
	}

	public static File createVideoThumbnail(MediaFile mediaFile, int frame, String size) {
		checkDir(THUMBNAILS_DIR + File.separator + SystemHelper.getFoldersForName(mediaFile.checksum));
		mediaFile.getStreams();
		String thumbPath = SystemHelper.getFolders(mediaFile.checksum) + File.separator + "thumb_0_"+size+".png";
		File thumb = new File(THUMBNAILS_DIR + File.separator + thumbPath);
		if(!thumb.exists() || mediaFile.cover == null) {
			File file = new File(STORAGE_FOLDER + File.separator + mediaFile.getLocation());
			Size s = getSize(size);
			thumb = FfmpegHelper.createScreenshot(file, thumb, s.w, frame);
			if(thumb != null) {
				String checksum = ThumbnailsHelper.getETag(thumb);
				Thumbnail.getOrCreate(mediaFile, thumbPath, checksum);
				if(mediaFile.cover == null) {
					mediaFile.cover = Thumbnail.getOrCreate(mediaFile, thumbPath, checksum);
					mediaFile.update();
				}
			}
		}
		return thumb;
	}
	
	public static String getETag(File file) {
		Object eTag = OpensslHelper.getMd5Checksum(file);
		return (String) eTag;
	}
	
	public static void checkThumbnailLocation(MediaFile mediaFile, File thumb, File oldThumb) {
		String checksum = ThumbnailsHelper.getETag(oldThumb);
		String thumbPath = SystemHelper.getFolders(mediaFile.checksum) + File.separator + thumb.getName();
		Thumbnail thumbnail = Thumbnail.Finder.where().eq("checksum", checksum).findUnique();
		if(thumbnail != null) {
			Logger.info("moving " + oldThumb.getAbsolutePath() + " to " + thumb.getAbsolutePath());
			try {
				FileUtils.moveFile(oldThumb, thumb);
				thumbnail.filepath = thumbPath.trim();
				thumbnail.update();
				FileUtils.deleteQuietly(oldThumb);
				FileUtils.deleteQuietly(oldThumb.getParentFile());
			} catch (IOException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
			Logger.info("deleting old Thumbnail " + oldThumb.getAbsolutePath());
		}		
	}
	
	public static void deleteThumbnails(MediaFile mediaFile) {
		File tn;
		Logger.info("deleting all Thumbnails of "+mediaFile.toString());
		mediaFile.cover = null;
		mediaFile.update();
		for(models.Thumbnail thumb : mediaFile.getThumbnails()) {
			tn = new File(thumb.filepath);
			if(tn.exists()) {
				FileUtils.deleteQuietly(tn);	
			}
			thumb.delete();
		}	
		tn = new File(STORAGE_FOLDER + File.separator + SystemHelper.getFoldersForName(mediaFile.checksum));
		if(tn.exists()) {
			FileUtils.deleteQuietly(tn);	
		}	
	}
	
	private static void checkDir(String path) {
		if (!new File(path).exists()) {
			new File(path).mkdirs();
		}
	}

	private static BufferedImage scale(BufferedImage source, int w, int h) {
		double xScale = (double) w / source.getWidth();
		double yScale = xScale;
		BufferedImage bi = getCompatibleImage(w, (int) (source.getHeight() * xScale));
		Graphics2D g2d = bi.createGraphics();
		
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.drawRenderedImage(source, at);
		g2d.dispose();
		return bi;
	}

	private static BufferedImage getCompatibleImage(int w, int h) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		return image;
	}
	
	private static Size getSize(String size) {
		String[] parts = size.split("x");
		Size s = new Size(0, 0);
		if(parts.length == 2) {
			try {
				s.w = Integer.parseInt(parts[0].trim());
				s.h = Integer.parseInt(parts[1].trim());
			} catch(NumberFormatException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		}
		return s;
	}
}

class Size {
	public int w;
	public int h;
	public Size(int w, int h) {
		this.w = w;
		this.h = h;
	}
}
