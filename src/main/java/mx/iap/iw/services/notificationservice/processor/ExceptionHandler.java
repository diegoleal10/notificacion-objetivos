package mx.iap.iw.services.notificationservice.processor;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.iap.iw.services.notificationservice.common.SystemConstants;
import mx.iap.iw.services.notificationservice.common.SystemProperties;

public class ExceptionHandler {

	private static final String TXT = ".txt";

	private static final String ERROR = "error-";

	private static final String FORMAT = "%s\r\n";

	private static Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

	private static final String URL_ERROR_FILE = SystemProperties.getProperty(SystemConstants.URL_ERROR_FILE);

	/**
	 * 
	 * @param body
	 * @return
	 */
	public Object processFileBody(@Body String body){
		log.error("Error en el envio des correo: " + body);
		
		Calendar cal = Calendar.getInstance();
		try (PrintWriter output = new PrintWriter(new FileWriter(generateFileName(cal), true))) {
			output.printf(FORMAT, body);
		} catch (Exception e) {
			log.error("Error generando archivo de error", e);
		}
		
		return body;
	}
	

	/**
	 * 
	 * @param cal
	 * @return
	 */
	private String generateFileName(Calendar cal) {
		return URL_ERROR_FILE + ERROR + cal.get(Calendar.YEAR) + cal.get(Calendar.MONTH) + cal.get(Calendar.DATE)
				+ TXT;
	}
}
