package mx.iap.iw.services.notificationservice.processor;

import static mx.iap.iw.services.notificationservice.common.SystemConstants.SERVICE_BODY_ERROR_MESSAGE;
import static mx.iap.iw.services.notificationservice.common.SystemConstants.SERVICE_EMPTY_ERROR_MESSAGE;
import static mx.iap.iw.services.notificationservice.common.SystemConstants.SERVICE_FIELD_ERROR_MESSAGE;
import static mx.iap.iw.services.notificationservice.common.SystemConstants.SERVICE_RANGE_ERROR_MESSAGE;
import static mx.iap.iw.services.notificationservice.common.SystemConstants.SERVICE_SHEETS_ERROR_MESSAGE;
import static mx.iap.iw.services.notificationservice.common.SystemConstants.SERVICE_SHEET_ID_ERROR_MESSAGE;
import static mx.iap.iw.services.notificationservice.common.SystemConstants.SERVICE_SUBJECT_ERROR_MESSAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import mx.iap.iw.services.notificationservice.common.SystemConstants;
import mx.iap.iw.services.notificationservice.common.SystemProperties;
import mx.iap.iw.services.notificationservice.common.util.ProcessUtil;
import mx.iap.iw.services.notificationservice.common.util.SheetsUtil;
import mx.iap.iw.services.notificationservice.datatransfer.EmailTemplateDTO;

/**
 * @author virginia https://developers.google.com/sheets/samples/formatting
 */
public class SheetProcessor implements Processor {

	private static final String BAJA = "BAJA";
	private static Logger log = LoggerFactory.getLogger(SheetProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		// Constants
		String body = ProcessUtil.validateSystemProperty(SystemConstants.BODY, null);
		String cc = ProcessUtil.validateSystemProperty(SystemConstants.DEFAULT_CC, "");
		String endLine = ProcessUtil.validateSystemProperty(SystemConstants.END_OF_LINE, "\\n");
		List<String> managerLabel = Arrays
				.asList(ProcessUtil.validateSystemProperty(SystemConstants.DEFAULT_MANAGER_LABEL, "xyz").split(","));
		String range = ProcessUtil.validateSystemProperty(SystemConstants.RANGE, null);
		String sheetsStr = ProcessUtil.validateSystemProperty(SystemConstants.SHEETS, null);
		String spreadsheetId = ProcessUtil.validateSystemProperty(SystemConstants.SHEET_ID, null);
		String subjectDefault = ProcessUtil.validateSystemProperty(SystemConstants.DEFAULT_TITLE, null);
		String type = ProcessUtil.validateSystemProperty(SystemConstants.AUTHORIZATION_TYPE, "app");

		// Maximo de registros vacios para finalizar
		int maxRowsEmpty = (SystemProperties.getProperty(SystemConstants.MAX_ROWS_EMPTY) != null
				&& !SystemProperties.getProperty(SystemConstants.MAX_ROWS_EMPTY).equals(""))
						? Integer.parseInt(SystemProperties.getProperty(SystemConstants.MAX_ROWS_EMPTY)) : 3;
		// Campo en donde esta la descripcion del area
		int mailFieldArea = (SystemProperties.getProperty(SystemConstants.MAIL_FIELD_AREA) != null
				&& !SystemProperties.getProperty(SystemConstants.MAIL_FIELD_AREA).equals(""))
						? Integer.parseInt(SystemProperties.getProperty(SystemConstants.MAIL_FIELD_AREA)) : -1;
		// Campo en donde se encuentra el nombre que se enviara en el body del
		// correo
		int mailFieldName = (SystemProperties.getProperty(SystemConstants.MAIL_FIELD_NAME) != null
				&& !SystemProperties.getProperty(SystemConstants.MAIL_FIELD_NAME).equals(""))
						? Integer.parseInt(SystemProperties.getProperty(SystemConstants.MAIL_FIELD_NAME)) : -1;
		// Campo en donde se encuentra la direccion de correo
		int mailFieldEmail = (SystemProperties.getProperty(SystemConstants.MAIL_FIELD_EMAIL) != null
				&& !SystemProperties.getProperty(SystemConstants.MAIL_FIELD_EMAIL).equals(""))
						? Integer.parseInt(SystemProperties.getProperty(SystemConstants.MAIL_FIELD_EMAIL)) : -1;
		// Campo en donde esta el mensaje que se quiere enviar
		int mailFieldBody = (SystemProperties.getProperty(SystemConstants.MAIL_FIELD_BODY) != null
				&& !SystemProperties.getProperty(SystemConstants.MAIL_FIELD_BODY).equals(""))
						? Integer.parseInt(SystemProperties.getProperty(SystemConstants.MAIL_FIELD_BODY)) : -1;
		// Registro en donde se encuentra el header del excel
		int mailRowHeader = (SystemProperties.getProperty(SystemConstants.MAIL_ROW_HEADER) != null
				&& !SystemProperties.getProperty(SystemConstants.MAIL_ROW_HEADER).equals(""))
						? Integer.parseInt(SystemProperties.getProperty(SystemConstants.MAIL_ROW_HEADER)) : -1;

		if (sheetsStr == null)
			throw new Exception(SERVICE_SHEETS_ERROR_MESSAGE);

		if (range == null)
			throw new Exception(SERVICE_RANGE_ERROR_MESSAGE);

		if (spreadsheetId == null)
			throw new Exception(SERVICE_SHEET_ID_ERROR_MESSAGE);

		if (subjectDefault == null)
			throw new Exception(SERVICE_SUBJECT_ERROR_MESSAGE);

		if (body == null)
			throw new Exception(SERVICE_BODY_ERROR_MESSAGE);

		if (mailFieldArea == -1 || mailFieldName == -1 || mailFieldEmail == -1 || mailFieldBody == -1 || mailRowHeader == -1)
			throw new Exception(SERVICE_FIELD_ERROR_MESSAGE);

		// Variables
		List<EmailTemplateDTO> data = new ArrayList<>();
		List<String> sheetsArray = Arrays.asList(sheetsStr.split(","));
		EmailTemplateDTO template = new EmailTemplateDTO();
		String subject = subjectDefault;
		String manager = null;
		int rowsEmpty = 0;

		// Build a new authorized API client service.
		Sheets service = SheetsUtil.getSheetsService(type);

		for (Iterator<String> iterator = sheetsArray.iterator(); iterator.hasNext();) {

			String sheet = (String) iterator.next();
			subject = subjectDefault;
			manager = (cc == null || cc.equals("")) ? "" : ";";
			rowsEmpty = 0;
			ValueRange response = service.spreadsheets().values().get(spreadsheetId, sheet + "!" + range).execute();

			List<List<Object>> values = response.getValues();

			if (values == null || values.size() == 0) {
				throw new Exception(SERVICE_EMPTY_ERROR_MESSAGE);

			} else {
				if (log.isDebugEnabled())
					log.debug("Sheet: " + sheet);

				subject += " (" + (values.get(mailRowHeader) != null && values.get(mailRowHeader).size() > 3
						&& values.get(mailRowHeader).get(mailFieldBody) != null ? values.get(mailRowHeader).get(mailFieldBody).toString()
								: "")
						+ ")"; // To obtain the date field

				values.remove(mailRowHeader); // remove the titles

				for (List<Object> row : values) {

					template = new EmailTemplateDTO();

					if (rowsEmpty < maxRowsEmpty) { // At least more than 3 columns

						if (row.size() > 3 && ((row.get(mailFieldName) != null && !row.get(mailFieldName).equals(""))
								&& (row.get(mailFieldEmail) != null && !row.get(mailFieldEmail).equals(""))
								&& (row.get(mailFieldBody) != null && !row.get(mailFieldBody).equals("")))) { // Complete data

							rowsEmpty = 0;

							if (row.get(mailFieldArea) != null && managerLabel.contains(row.get(mailFieldArea))) {

								boolean validateEmail = ProcessUtil.validateEmail(row.get(mailFieldEmail).toString());
								if (validateEmail) {
									manager += row.get(mailFieldName).toString() + "<" + row.get(mailFieldEmail).toString() + ">;";
									if (log.isDebugEnabled())
										log.debug("*** Gerente: " + row.get(mailFieldName).toString());
								} else {
									if (log.isDebugEnabled())
										log.debug("*** Gerente-INVALID Email: " + row.get(mailFieldName).toString() + ", "
												+ row.get(mailFieldEmail).toString());
								}
							}

							if (managerLabel.contains(row.get(mailFieldArea))) {
								if (log.isDebugEnabled())
									log.debug("******************* Gerente o Lider: " + row.get(mailFieldArea));
								template.setCc(cc);
							} else {
								if (log.isDebugEnabled())
									log.debug("******************* NO Gerente **********************");
								template.setCc(cc + manager);
							}

							template.setName(row.get(mailFieldName).toString());

							boolean validateEmail = ProcessUtil.validateEmail(row.get(mailFieldEmail).toString());

							if (validateEmail && !row.get(mailFieldArea).toString().equalsIgnoreCase(BAJA)) {

								// Concat username and email
								template.setTo(row.get(mailFieldName).toString() + "<" + row.get(mailFieldEmail).toString() + ">");

								// Objects to convert in a list
								String objects = row.get(mailFieldBody).toString();
								String liList = "";
								List<String> objecsArray = Arrays.asList(objects.split(endLine));
								for (String string : objecsArray) {
									String object = "<li>" + string + "</li>";
									liList += object;
								}
								String objectsUl = "<ul>" + liList + "</ul>";

								// Replace the template
								template.setBody(String.format(body, row.get(mailFieldName), objectsUl));
								// template.setBody(row.get(3).toString());

								if (log.isDebugEnabled())
									log.debug("++++++ " + template);

								data.add(template);

							} else {
								if (log.isDebugEnabled())
									log.debug("++++++ INVALID Email: " + row.get(mailFieldEmail).toString());
							}

						} else {
							rowsEmpty++;
						}

					} else {
						// more than three sheets empty or with incomplete data,
						// next sheet
						break;
					}
				}
			}
		}
		
		exchange.getOut().setHeader("subject", subject);
		exchange.getOut().setBody(data, List.class);
	}

}