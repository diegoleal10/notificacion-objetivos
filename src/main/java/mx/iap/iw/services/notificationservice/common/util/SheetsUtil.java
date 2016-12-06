package mx.iap.iw.services.notificationservice.common.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;

import mx.iap.iw.services.notificationservice.common.SystemConstants;

import com.google.api.services.sheets.v4.Sheets;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SheetsUtil {

	private static final Logger LOGGER = Logger.getLogger(SheetsUtil.class.getName());

	/** Application name. */
	private static final String APPLICATION_NAME =  ProcessUtil.validateSystemProperty(SystemConstants.APLICATION_NAME, "");

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			ProcessUtil.validateSystemProperty(SystemConstants.CLIENT_STORE_CREDENTIAL, ""));
	
	/** Global instance of the secret. */
	private static final String CLIENT_SECRETS_LOCATION =  ProcessUtil.validateSystemProperty(SystemConstants.CLIENT_SECRET_URL, "");

	/** Global instance of the service account. */
	private static final String SERVICE_ACCOUNT_ID = ProcessUtil.validateSystemProperty(SystemConstants.SERVICE_ACCOUNT_ID, "");

	/** Global instance of the client account user. */
	private static final String SERVICE_ACCOUNT_USER = ProcessUtil.validateSystemProperty(SystemConstants.SERVICE_ACCOUNT_USER, "");

	/** Global instance of the client account user. */
	private static final String SERVICE_URL_KEY =  ProcessUtil.validateSystemProperty(SystemConstants.SERVICE_URL_KEY, "");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/sheets.googleapis.com-java-quickstart.json
	 */
	private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @return an authorized Credential object.
	 * @throws Exception
	 */
	public static Credential authorize() throws Exception {

		// Load client secrets.
		InputStream in = new FileInputStream(CLIENT_SECRETS_LOCATION);

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline")
						.setApprovalPrompt("force").build();

		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
				.authorize(SERVICE_ACCOUNT_USER);

		LOGGER.log(Level.INFO, "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());

		return credential;
	}

	/**
	 * Validate and create an authorized Credential object.
	 * 
	 * @return an authorized Credential object.
	 * @throws Exception
	 */
	public static Credential authorizeService() throws Exception {
		
		GoogleCredential credential = new GoogleCredential.Builder()
				.setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY)
				.setServiceAccountId(SERVICE_ACCOUNT_ID)
				.setServiceAccountPrivateKeyFromP12File(new File(SERVICE_URL_KEY))
				.setServiceAccountScopes(SCOPES)
				.setServiceAccountUser(SERVICE_ACCOUNT_USER)
				.build();

		return credential;

	}

	/**
	 * Build and return an authorized Sheets API client service.
	 * 
	 * @param the
	 *            type of authorization
	 * @return an authorized Sheets API client service
	 * @throws Exception
	 */
	public static Sheets getSheetsService(String type) throws Exception {

		Credential credential = null;

		switch (type) {
		case "service":
			credential = authorizeService();
			break;
		case "app":
			credential = authorize();
			break;
		default:
			credential = authorize();
			break;
		}

		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

}
