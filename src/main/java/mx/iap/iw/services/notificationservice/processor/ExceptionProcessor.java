package mx.iap.iw.services.notificationservice.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionProcessor implements Processor {
	private static Logger log = LoggerFactory.getLogger(ExceptionProcessor.class);
	
	@Override
	public void process(Exchange exchange)
	throws Exception {
		log.error("Exception: " + exchange.getProperty(Exchange.EXCEPTION_CAUGHT));
	}
}
