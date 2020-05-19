package org.shanoir.downloader;

import org.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.MethodExpression;
import org.jboss.seam.security.Identity;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;

import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.utils.Util;
import org.shanoir.util.ShanoirUtil;
import org.springframework.http.HttpHeaders;
/**
 * This class intends to be used as a binary executable to download datasets
 * from a remote Shanoir server to the local file system.
 *
 * @author aferial
 */
public final class ShanoirDownloader extends ShanoirCLI {

	/** -datasetId to set the id of the dataset to download. */
	private static Option datasetIdOption;

	/** -subjectId to set the id of the subject to download. */
	private static Option subjectIdOption;

	/** -studyId to set the id of the study to download. */
	private static Option studyIdOption;

	/** The Constant DESCRIPTION. */
	private static final String DESCRIPTION = "Import dataset with the specified ID to the given destination dir.\n"
			+ "Options:";

	/** -destDir to set the destination directory. */
	private static Option destDirOption;

	/** The Constant EXAMPLE. */
	private static final String EXAMPLE = "downloadDataset -destDir /tmp/dataset123 -port 8080 -host 127.0.0.1 -datasetId 123\n"
			+ "=> download the dataset 123 to the destination directory /tmp/dataset123.";
	/**
	 * -refDatasetExpressionFormatId to set the id of the ref dataset expression
	 * format.
	 */
	private static Option formatIdOption;

	/** -h used to request help on command line options. */
	private static Option helpOption;
	/** The Constant USAGE. */
	private static final String USAGE = "downloadDataset [Options] -datasetId <ID> -host <HOST> -port <PORT>";

	/** -v returns the version of the application. */
	private static Option versionOption;
	static {
		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("Print help for this application");
		helpOption = OptionBuilder.create("h");
	}

	static {
		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("print the version information and exit");
		versionOption = OptionBuilder.create("v");
	}
	static {
		OptionBuilder.withArgName("datasetId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("The dataset id.");
		datasetIdOption = OptionBuilder.create("datasetId");
	}

	static {
		OptionBuilder.withArgName("subjectId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("The subject id.");
		subjectIdOption = OptionBuilder.create("subjectId");
	}

	static {
		OptionBuilder.withArgName("studyId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("The study id.");
		studyIdOption = OptionBuilder.create("studyId");
	}

	static {
		OptionBuilder.withArgName("formatId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder
				.withDescription("The ref dataset expression format id. Default is Nifti. User ListReference service to see available RefDatasetExpressionFormats.");
		formatIdOption = OptionBuilder.create("formatId");
	}
	static {
		OptionBuilder.withArgName("destDir");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Destination directory, "
				+ SystemUtils.JAVA_IO_TMPDIR + " by default.");
		destDirOption = OptionBuilder.create("destDir");
	}
	
	/**
	 * Main method.
	 *
	 * @param args
	 *            the args
	 *
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	public static void main(final String[] args)
			throws ParserConfigurationException {

		if(java.util.Arrays.asList(args).contains("-sslDisableVerifier")){
			// HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
	            public boolean verify(String s, SSLSession sslSession) {
	                return true;
	            }
	        });
			
		}

		// Define the options needed in addition of those from the generic
		// ShanoirTkCLI
		Options opts = new Options();
		opts.addOption(helpOption);
		opts.addOption(versionOption);
		opts.addOption(datasetIdOption);
		opts.addOption(subjectIdOption);
		opts.addOption(studyIdOption);
		opts.addOption(destDirOption);
		opts.addOption(formatIdOption);

		ShanoirDownloader shanoirDownloader = new ShanoirDownloader(opts);

		try {
			shanoirDownloader.parse(args);

			shanoirDownloader.download();

		} catch (MissingArgumentException e) {
			exit(e.getMessage());
		} catch (java.text.ParseException e) {
			exit(e.getMessage());
		} catch (DatatypeConfigurationException e) {
			exit(e.getMessage());
		}

	}

	// private void keycloakDesktopAuthentification() {
	// 	try {
	// 		FileInputStream fIS = new FileInputStream(ShUpConfig.keycloakJson);
	// 		KeycloakInstalled keycloakInstalled = new KeycloakInstalled(fIS);
	// 		keycloakInstalled.setLocale(Locale.ENGLISH);
	// 		keycloakInstalled.loginDesktop();
	// 		ShUpOnloadConfig.setKeycloakInstalled(keycloakInstalled);
	// 	} catch (Exception e) {
	// 		System.out.println(e.getMessage());
	// 	}
	// }

	private void keycloakAuthentification(String username, String password) {
		try {

			List<String> keycloakJsonString = Files.readAllLines(ShUpConfig.keycloakJson.toPath());
			JSONObject keycloakJson = new JSONObject(String.join("", keycloakJsonString));

			String keycloakAuthenticationURL = keycloakJson.getString("auth-server-url");

			keycloakAuthenticationURL += "/realms/" + keycloakJson.getString("realm") + "/protocol/openid-connect/token";

			SSLContext sslContext = null;
			try {
				sslContext = SSLContexts.custom().useTLS().build();
			} catch (GeneralSecurityException e) {
				log.error("Error during ssl context initialization", e);
			}

			final SSLConnectionSocketFactory f = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1",
					"TLSv1.1", "TLSv1.2" }, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

			final CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(f).build();
			final HttpPost httpost = new HttpPost(keycloakAuthenticationURL);
			StringEntity se = null;
			try {
				final StringBuilder str = new StringBuilder();
				str.append("client_id=shanoir-uploader");
				str.append("&grant_type=password");
				str.append("&username=").append(username);
				str.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
				// str.append("&scope=openid info");
				 str.append("&scope=openid info offline_access");
				se = new StringEntity(str.toString());
			} catch (UnsupportedEncodingException e) {
				log.error("Keycloak authentication. Unsupported encoding exception on entity creation", e);
			}

			httpost.setEntity(se);
			httpost.setHeader("Content-type", "application/x-www-form-urlencoded");

			CloseableHttpResponse response = null;
			try {
				response = httpclient.execute(httpost);

				String responseEntityString = EntityUtils.toString(response.getEntity());
				
				final int statusCode = response.getStatusLine().getStatusCode();
				if (HttpStatus.SC_OK == statusCode) {
					JSONObject responseEntityJson = new JSONObject(responseEntityString);					
					ShUpOnloadConfig.tokenString = responseEntityJson.getString("access_token");
				}
			} catch (ClientProtocolException e) {
				log.error("Keycloak is unreachable. Client protocol exception", e);
			} catch (IOException e) {
				log.error("Keycloak is unreachable. IO exception", e);
			} finally {
				try {
					httpclient.close();
					response.close();
				} catch (Exception e) {
					log.error("There was an error closing the Keycloak connection", e);
				}
			}

			// return false;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/** Our business Service. */
	// private Downloader downloader;
	private ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	/**
	 * @param opts
	 *            the specific options of the command line
	 */
	public ShanoirDownloader(final Options opts) {
		super(opts, DESCRIPTION, EXAMPLE, USAGE);
		initShanoirUploaderFolder();
		initProperties(ShUpConfig.BASIC_PROPERTIES, ShUpConfig.basicProperties);

		String shanoirNgLocalProfile = "sh-ng-localhost";
		String filePath = File.separator + ShUpConfig.PROFILE_DIR + shanoirNgLocalProfile;
		ShUpConfig.profileDirectory = new File(ShUpConfig.shanoirUploaderFolder, filePath);

		File profilePropertiesFile = new File(ShUpConfig.profileDirectory, ShUpConfig.PROFILE_PROPERTIES);
		loadPropertiesFromFile(ShUpConfig.profileProperties, profilePropertiesFile);

		// put settings into ShUpOnloadConfig for sh-ng
		ShUpOnloadConfig.setShanoirNg(Boolean.parseBoolean(ShUpConfig.profileProperties.getProperty("is.ng.up")));
		File keycloakJson = new File(ShUpConfig.profileDirectory, ShUpConfig.KEYCLOAK_JSON);
		if (keycloakJson.exists()) {
			ShUpConfig.keycloakJson = keycloakJson;
		} else {
			if (ShUpOnloadConfig.isShanoirNg()) {
				System.err.println("Error: missing keycloak.json! Connection with sh-ng will not work.");
			}
		}
	}
	
	private void initShanoirUploaderFolder() {
		final String userHomeFolderPath = System.getProperty(ShUpConfig.USER_HOME);
		final String shanoirUploaderFolderPath = userHomeFolderPath
				+ File.separator + ShUpConfig.SU + "_" + ShUpConfig.SHANOIR_UPLOADER_VERSION;
		final File shanoirUploaderFolder = new File(shanoirUploaderFolderPath);
		boolean shanoirUploaderFolderExists = shanoirUploaderFolder.exists();
		if (shanoirUploaderFolderExists) {
			// do nothing
		} else {
			shanoirUploaderFolder.mkdirs();
		}
		ShUpConfig.shanoirUploaderFolder = shanoirUploaderFolder;
	}
	
	/**
	 * Reads properties from .su folder into memory, or copies property file if not existing.
	 */
	private void initProperties(final String fileName, final Properties properties) {
		final File propertiesFile = new File(ShUpConfig.shanoirUploaderFolder + File.separator + fileName);
		if (propertiesFile.exists()) {
			// do nothing
		} else {
			Util.copyFileFromJar(fileName, propertiesFile);
		}
		loadPropertiesFromFile(properties, propertiesFile);
	}

	private void loadPropertiesFromFile(final Properties properties, final File propertiesFile) {
		try {
			final FileInputStream fIS = new FileInputStream(propertiesFile);
			properties.load(fIS);
			fIS.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void downloadDataset(File destDir, Long datasetId, String format, ShanoirUploaderServiceClientNG shng) throws Exception {
		HttpResponse response = shng.downloadDatasetById(datasetId, format);

		Header header = response.getFirstHeader(HttpHeaders.CONTENT_DISPOSITION);
		String fileName = header.getValue();
		fileName = fileName.replace("attachment;filename=", "");
		
		final File downloadedFile = new File(destDir + "/" + fileName);

		FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
	}

	public static void downloadDatasetByStudy(File destDir, Long studyId, String format, ShanoirUploaderServiceClientNG shng) throws Exception {
		List<Long> datasetIds = shng.findDatasetIdsByStudyId(studyId);
		for(Long datasetId : datasetIds) {
			downloadDataset(destDir, datasetId, format, shng);
		}
	}

	public static void downloadDatasetBySubject(File destDir, Long subjectId, String format, ShanoirUploaderServiceClientNG shng) throws Exception {
		List<Long> datasetIds = shng.findDatasetIdsBySubjectId(subjectId);
		for(Long datasetId : datasetIds) {
			downloadDataset(destDir, datasetId, format, shng);
		}
	}

	public static void downloadDatasetByStudyAndSubject(File destDir, Long subjectId, Long studyId, String format, ShanoirUploaderServiceClientNG shng) throws Exception {
		List<Long> datasetIds = shng.findDatasetIdsBySubjectIdStudyId(subjectId, studyId);
		for(Long datasetId : datasetIds) {
			downloadDataset(destDir, datasetId, format, shng);
		}
	}

	/**
	 * This method download Dataset corresponding to the properties set by the
	 * user.
	 */
	private void download() {

		File destDir = new File(SystemUtils.JAVA_IO_TMPDIR);
		if (cl.hasOption("destDir")) {
			destDir = new File(cl.getOptionValue("destDir"));
			if (!destDir.isDirectory()) {
				throw new IllegalArgumentException("Destination is not a directory! destDir: " + destDir);
			}
		}

		keycloakAuthentification(cl.getOptionValue("user"), cl.getOptionValue("password"));
		
		shanoirUploaderServiceClientNG = new ShanoirUploaderServiceClientNG();
		
		ShUpConfig.profileProperties.setProperty("shanoir.server.url", "http://" + getHost() + ":" + getPort());
		
		try {
			String format = "nii";

			if (cl.hasOption("formatId")) {
				format = Long.parseLong(cl.getOptionValue("formatId")) == 6 ? "dcm" : "nii";
			}

			if (cl.hasOption("datasetId")) {
				Long datasetId = Long.parseLong(cl.getOptionValue("datasetId"));
				downloadDataset(destDir, datasetId, format, shanoirUploaderServiceClientNG);
			}

			if (cl.hasOption("studyId") && !cl.hasOption("subjectId")) {
				Long studyId = Long.parseLong(cl.getOptionValue("studyId"));
				downloadDatasetByStudy(destDir, studyId, format, shanoirUploaderServiceClientNG);
			}

			if (cl.hasOption("subjectId") && !cl.hasOption("studyId")) {
				Long subjectId = Long.parseLong(cl.getOptionValue("subjectId"));
				downloadDatasetBySubject(destDir, subjectId, format, shanoirUploaderServiceClientNG);
			}

			if (cl.hasOption("subjectId") && cl.hasOption("studyId")) {
				Long studyId = Long.parseLong(cl.getOptionValue("studyId"));
				Long subjectId = Long.parseLong(cl.getOptionValue("subjectId"));
				downloadDatasetByStudyAndSubject(destDir, studyId, subjectId, format, shanoirUploaderServiceClientNG);
			}
	       
		} catch (NumberFormatException e) {
			e.printStackTrace();
			exit("Download failed: could not parse your dataset, subject or study id, make sure it only contains numbers.");
		} catch (Exception e) {
			e.printStackTrace();
			exit("Download failed: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.toolkit.ShanoirTkCLI#postParse()
	 */
	@Override
	protected void postParse() throws MissingArgumentException,
			DatatypeConfigurationException {
		if (cl.hasOption("studyId") && cl.hasOption("subjectId") && cl.hasOption("datasetId")) {
			exit("Either -datasetId -subjectId or -studyId is required");
		}
	}

}