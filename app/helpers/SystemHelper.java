package helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SystemHelper {
	private SystemHelper() {
	}

	public static String runCommand(String cmd) {
		StringBuilder sb = new StringBuilder();
		try {
			String line;
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				sb.append(line.trim()).append("\n");
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
		return sb.toString();
	}
}
