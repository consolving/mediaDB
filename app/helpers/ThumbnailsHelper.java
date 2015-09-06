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

import com.typesafe.config.ConfigFactory;

import models.MediaFile;
import models.Thumbnail;
import play.Logger;
import play.cache.Cache;

public class ThumbnailsHelper {
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String THUMBNAILS_DIR = ROOT_DIR + File.separator + "thumbnails";
	private final static String STORAGE_FOLDER = ROOT_DIR + File.separator + "storage";
	
	private ThumbnailsHelper() {

	}

	public static File createThumbnail(MediaFile mediaFile, String size) {
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
		checkDir(THUMBNAILS_DIR + File.separator + mediaFile.checksum);
		File thumb = new File(THUMBNAILS_DIR + File.separator + mediaFile.checksum + File.separator + "thumb_0_"+size+".png");
		if(!thumb.exists()) {
			File file = new File(STORAGE_FOLDER + File.separator + mediaFile.checksum);
			if(file.exists()) {
				try {
					BufferedImage img = ImageIO.read(file);
					Size s = getSize(size);
					BufferedImage scaled = scale(img, s.w, s.h);
					ImageIO.write(scaled, "png", thumb);
					Thumbnail.getOrCreate(mediaFile, thumb.getAbsolutePath());
				} catch (IOException ex) {
					Logger.warn(ex.getLocalizedMessage(), ex);
				} catch(java.lang.ArrayIndexOutOfBoundsException ex ) {
					Logger.warn(ex.getLocalizedMessage(), ex);					
				}
			} else {
				Logger.warn(file.getAbsolutePath()+ " does not exists!");
			}
		}
		return thumb;
	}

	public static File createVideoThumbnail(MediaFile mediaFile, int frame, String size) {
		checkDir(THUMBNAILS_DIR + File.separator + mediaFile.checksum);
		File thumb = new File(THUMBNAILS_DIR + File.separator + mediaFile.checksum + File.separator + "thumb_0_"+size+".png");
		if(!thumb.exists()) {
			File file = new File(STORAGE_FOLDER + File.separator + mediaFile.checksum);
			Size s = getSize(size);
			thumb = FfmpegHelper.createScreenshot(file, thumb, s.w, frame);
			if(thumb != null) {
				Thumbnail.getOrCreate(mediaFile, thumb.getAbsolutePath());
			}
		}
		return thumb;
	}

	public static String getETag(File thumbnail) {
		Object eTag = Cache.get(thumbnail.getName());
		if(eTag == null) {
			eTag = OpensslHelper.getMd5Checksum(thumbnail);
			Cache.set(thumbnail.getName(), eTag, 60*60);
		}
		return (String) eTag;
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
