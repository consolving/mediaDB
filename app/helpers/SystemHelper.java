package helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.typesafe.config.ConfigFactory;

import play.Logger;

public class SystemHelper {
	private final static String SYSTEM_bash_BIN = ConfigFactory.load().getString("system.bash.bin");

	private SystemHelper() {
	}

	public static String runCommand(String cmd) {
		StringBuilder sb = new StringBuilder();
		try {
			ProcessBuilder pb = new ProcessBuilder(SYSTEM_bash_BIN, "-c", cmd);
			String line;
			Process p = pb.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				if(line != null && line.toLowerCase().trim().contains("error")) {
					Logger.error(line.trim());
				}
				sb.append(line.trim()).append("\n");
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
		return sb.toString();
	}
}
