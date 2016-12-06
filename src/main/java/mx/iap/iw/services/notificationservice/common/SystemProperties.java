package mx.iap.iw.services.notificationservice.common;

import java.io.FileInputStream;
import java.io.InputStream;

public class SystemProperties {
	private static java.util.Properties properties;
		
	static {
		init();
	}
	
	private static void init() {
		try (InputStream file =
				new FileInputStream(
					SystemConstants.SYSTEM_PROPERTIES_FILE_NAME)) {
			properties = new java.util.Properties();
			properties.load(file);
		} catch (Exception ioE) {
			System.err.println(ioE.toString());
		}
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
