package mx.iap.iw.services.notificationservice.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mx.iap.iw.services.notificationservice.common.SystemConstants;
import mx.iap.iw.services.notificationservice.common.SystemProperties;

/**
 * Clase Util para la validacion de email y otros campos
 * 
 * @author vleon
 *
 */
public final class ProcessUtil {
	
    private static final String PATTERN_EMAIL = (SystemProperties.getProperty(SystemConstants.MAIL_FIELD_PATTERN) != null
			&& !SystemProperties.getProperty(SystemConstants.MAIL_FIELD_PATTERN).equals(""))
			? SystemProperties.getProperty(SystemConstants.MAIL_FIELD_PATTERN) : "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
    /**
     * Validate given email with regular expression.
     * 
     * @param email email for validation
     * @return true valid email, otherwise false
     */
    public static boolean validateEmail(String email) {
 
        // Compiles the given regular expression into a pattern.
        Pattern pattern = Pattern.compile(PATTERN_EMAIL);
 
        // Match the given input against this pattern
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
 
    }
    
    /**
     * 
     * @param property propiedad que deb ser obtenida
     * @param valueDefault valor default 
     * @return propiedad configurada en el .properties
     */
	public static String validateSystemProperty(String property, String valueDefault){
		
		return (SystemProperties.getProperty(property) != null
					&& !SystemProperties.getProperty(property).equals(""))
							? SystemProperties.getProperty(property) : valueDefault;
	}
}
