package helpers;

import java.io.BufferedReader;
import java.io.File;
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
	
	public static String getFoldersForName(String name) {
		return calcFolder(name, true);
	}

	public static String getFolders(String name) {
		return calcFolder(name, false);
	}
	
	private static String calcFolder(String name, boolean file) {
		if(name == null){
			return name;
		}
		int l = name.length();
		int div = 8;
		int count = l/div;
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i <= count; i++){
			sb.append(name.substring((i-1)*div, i*div));
			sb.append(i != count ? File.separator : "");
		}
		if(file) {
			sb.append(File.separator);
			sb.append(name);
		}
		return sb.toString();
	}
}
