package helpers;

import java.io.File;

import com.typesafe.config.ConfigFactory;

import play.Logger;

public class FfmpegHelper {
	private final static String FFMPEG_BIN = ConfigFactory.load().getString("media.ffmpeg.bin");
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

}
