package mx.iap.iw.services.notificationservice.route;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import mx.iap.iw.services.notificationservice.processor.ExceptionProcessor;

/**
 * 
 * Clase base para la configuracion de rutas camel.
 * Configuracion de exceptionProcessor para manejo de errores 
 * 
 * @author vleon
 *
 */
public class BaseRoute extends RouteBuilder {
	private final Processor exceptionProcessor = new ExceptionProcessor();
	
	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler());
		onException(Exception.class).process(exceptionProcessor).handled(true);
	}
}
