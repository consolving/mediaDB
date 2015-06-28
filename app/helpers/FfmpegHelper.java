package helpers;

import com.typesafe.config.ConfigFactory;

public class FfmpegHelper {
	private final static String FFMPEG_BIN = ConfigFactory.load().getString("media.ffmpeg.bin");
	private final static String FFPROBE_BIN = ConfigFactory.load().getString("media.ffprobe.bin");

	private FfmpegHelper() {}
	
}
