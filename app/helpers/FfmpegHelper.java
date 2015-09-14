package helpers;

import java.io.File;

import com.typesafe.config.ConfigFactory;

import play.Logger;

public class FfmpegHelper {
	private final static String FFMPEG_BIN = ConfigFactory.load().getString("media.ffmpeg.bin");
	private final static boolean HAS_FFMPEG_BIN = new File(FFMPEG_BIN).exists();
	private final static String FFPROBE_BIN = ConfigFactory.load().getString("media.ffprobe.bin");
	private final static boolean HAS_FFPROBE_BIN = new File(FFPROBE_BIN).exists();

	private FfmpegHelper() {
	}

	public static String getFileProperties(File file) {
		if (HAS_FFPROBE_BIN && file.exists()) {
			String cmd = FFPROBE_BIN + " -v quiet -print_format json -show_format -show_streams \""+ file.getAbsolutePath() + "\"";
			Logger.debug("running: "+cmd);
			return SystemHelper.runCommand(cmd);
		}
		return null;
	}

	/**
	 * 
	 * @param file
	 * @param thumb
	 * @param width
	 * @param frame
	 * @return
	 */
	public static File createScreenshot(File file, File thumb, int width, int frame) {
		if(HAS_FFMPEG_BIN && file != null && file.exists()) {
			String cmd = FFMPEG_BIN + " -y -ss "+frame+" -i \""+ file.getAbsolutePath() + "\" -vf \"scale='min(iw,"+width+")':-1\" -vframes 1 \""+ thumb.getAbsolutePath() + "\"";
			Logger.debug("running: "+cmd);	
			SystemHelper.runCommand(cmd);
		}
		return thumb != null && thumb.exists() ? thumb : null;
	}
}
