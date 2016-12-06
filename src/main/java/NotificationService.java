import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.iap.iw.services.notificationservice.route.NotificationRoute;

/**
 * Clase inicial de ejecucion de las rutas camel
 * 
 * @author vleon
 *
 */
public class NotificationService {

	private static Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRoute notificationRoute = new NotificationRoute();

	private static JndiRegistry registry;
	private static CamelContext context;

	private void init() throws Exception {

		registry = new JndiRegistry(true);

		context = new DefaultCamelContext(registry);
		context.addRoutes(notificationRoute);

		context.start();

	}

	public static void main(String... args) {

		log.info("Configuracion de rutas Camel");

		NotificationService notificationService = new NotificationService();

		try {
			notificationService.init();
		} catch (Exception e) {
			log.error("Error en configuracion de rutas camel", e);
		}
	}
}
