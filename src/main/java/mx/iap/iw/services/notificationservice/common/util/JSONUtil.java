package mx.iap.iw.services.notificationservice.common.util;

import com.google.gson.Gson;

public final class JSONUtil {
	private static final Gson GSON = new Gson();
	
	public static <T> T parse(String jsonString, Class<T> objectClass) {
		return GSON.fromJson(jsonString, objectClass);
	}
	
	public static String parse(Object objectClass) {
		return GSON.toJson(objectClass);
	}
}
