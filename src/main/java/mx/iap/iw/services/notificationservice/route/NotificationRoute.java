package mx.iap.iw.services.notificationservice.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.iap.iw.services.notificationservice.common.SystemConstants;
import mx.iap.iw.services.notificationservice.common.SystemProperties;
import mx.iap.iw.services.notificationservice.datatransfer.EmailTemplateDTO;
import mx.iap.iw.services.notificationservice.processor.ExceptionHandler;
import mx.iap.iw.services.notificationservice.processor.SheetProcessor;

/**
 * Ruta camel para notificaciones de objetivos semanales
 * Se configuran processor para google sheet
 * Se configura manejo de excepcion para errores SMTP
 * Se configuran rutas para envio de correo en caso de error SMTP
 * 
 * @author vleon
 *
 */
public class NotificationRoute extends BaseRoute {
	
	private static Logger log = LoggerFactory.getLogger(NotificationRoute.class);

	private static final String SUBJECT_ERROR_MAIL = SystemProperties.getProperty(SystemConstants.SUBJECT_ERROR_MAIL);

	/** Global instance of the notification route. */
	private static final String NOTIFICATION_ROUTE = "notification";

	/** Global constant of the client account user. */
	private static final String SERVICE_ACCOUNT_USER = SystemProperties.getProperty(SystemConstants.SERVICE_ACCOUNT_USER);

	/** Global constant of the client account name. */
	private static final String SERVICE_ACCOUNT_NAME = SystemProperties.getProperty(SystemConstants.SERVICE_ACCOUNT_NAME);

	/** Global constant of the client pass. */
	private static final String SERVICE_ACCOUNT_KEY = SystemProperties.getProperty(SystemConstants.SERVICE_ACCOUNT_KEY);

	/** Global constant of the quartz. */
	private static final String QUARTZ2_CONFIG = SystemProperties.getProperty(SystemConstants.QUARTZ2);

	/** Global constant of the quartz. */
	private static final String DEBUG_MODE = SystemProperties.getProperty(SystemConstants.DEBUG_MODE);

	/** Global constant of the quartz. */
	private static final String URL_FILE = SystemProperties.getProperty(SystemConstants.URL_FILE);

	private static final String TO_ERROR = SystemProperties.getProperty(SystemConstants.TO_ERROR);

	/** Global instance of the Sheet Processor. */
	private final SheetProcessor SHEET_PROCESSOR = new SheetProcessor();

	@Override
	public void configure() throws Exception {
		super.configure();

		log.info("Inicio procesamiento de notificaciones");

		onException(Exception.class)
			.handled(true)
			.bean(ExceptionHandler.class,"processFileBody")
			.to("direct:prepareErrorEmail");

		// Ruta de procesamiento del Google Sheet
		from("quartz2://iw-group/notif-timer?cron=" + QUARTZ2_CONFIG)
			.routeId(NOTIFICATION_ROUTE)
			.process(SHEET_PROCESSOR).process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {

						@SuppressWarnings("unchecked")
						List<EmailTemplateDTO> emailLst = exchange.getIn().getBody(List.class);
						ProducerTemplate template = exchange.getContext().createProducerTemplate();

						String from = SERVICE_ACCOUNT_NAME + "<" + SERVICE_ACCOUNT_USER + ">";

						for (EmailTemplateDTO email : emailLst) {

							// send email with a body and header
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("From", from);
							map.put("To", email.getTo());
							map.put("Cc", email.getCc());
							map.put("Subject", exchange.getIn().getHeader("subject"));

							template.sendBodyAndHeaders("seda:sendEmail", email.getBody(), map);
						}

						exchange.getOut().setBody(emailLst.toString(), String.class);
					}
				});

		from("seda:sendEmail")
//			.to("smtps://smtp.gmail.com?username=" + SERVICE_ACCOUNT_USER + "&password=" + SERVICE_ACCOUNT_KEY
//						+ "&contentType=text/html&debugMode=" + DEBUG_MODE)
			.to("file:" + URL_FILE + "?fileName=reportSend-$simple{date:now:yyyyMMdd}.txt&fileExist=append")
			.to("log:mx.iap.iw?level=INFO&showHeaders=true");

		// Ruta para envio de correo en caso de error SMTP
		from("direct:prepareErrorEmail")
			.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {

				ProducerTemplate template = exchange.getContext().createProducerTemplate();

				String from = SERVICE_ACCOUNT_NAME + "<" + SERVICE_ACCOUNT_USER + ">";

				// send email with a body and header
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("From", from);
				map.put("To", TO_ERROR);
				map.put("Subject", SUBJECT_ERROR_MAIL);
				
				Object body = exchange.getIn().getBody();
				
				template.sendBodyAndHeaders("seda:sendErrorEmail", body, map);

			}
		});
		
		from("seda:sendErrorEmail")
//			.to("smtps://smtp.gmail.com?username=" + SERVICE_ACCOUNT_USER + "&password=" + SERVICE_ACCOUNT_KEY
//					+ "&contentType=text/html&debugMode=" + DEBUG_MODE)
			.to("log:mx.iap.iw?level=INFO");

	}
}
