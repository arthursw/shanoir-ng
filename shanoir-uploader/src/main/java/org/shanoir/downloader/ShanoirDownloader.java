package org.shanoir.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
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
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.utils.Util;
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
		OptionBuilder.isRequired();
		OptionBuilder.withDescription("The dataset id.");
		datasetIdOption = OptionBuilder.create("datasetId");
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
	
	

	/** The dataset ID to be downloaded. */
	private long datasetId;

	/** The destination directory. */
	private File destDir = new File(SystemUtils.JAVA_IO_TMPDIR);

	private void keycloakAuthentification() {
		try {
			FileInputStream fIS = new FileInputStream(ShUpConfig.keycloakJson);
			KeycloakInstalled keycloakInstalled = new KeycloakInstalled(fIS);
			keycloakInstalled.setLocale(Locale.ENGLISH);
			keycloakInstalled.loginDesktop();
			ShUpOnloadConfig.setKeycloakInstalled(keycloakInstalled);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/** Our business Service. */
	// private Downloader downloader;
	private ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	/** The ref dataset expression format ID of the Dataset to be downloaded. */
	private long formatId;

	/**
	 * @param opts
	 *            the specific options of the command line
	 */
	public ShanoirDownloader(final Options opts) {
		super(opts, DESCRIPTION, EXAMPLE, USAGE);
		initShanoirUploaderFolder();
		initProperties(ShUpConfig.BASIC_PROPERTIES, ShUpConfig.basicProperties);
		System.out.println("basic.properties successfully initialized.");

		String shanoirNgLocalProfile = "sh-ng-localhost";
		String filePath = File.separator + ShUpConfig.PROFILE_DIR + shanoirNgLocalProfile;
		ShUpConfig.profileDirectory = new File(ShUpConfig.shanoirUploaderFolder, filePath);
		System.out.println("Profile directory set to: " + ShUpConfig.profileDirectory.getAbsolutePath());
		File profilePropertiesFile = new File(ShUpConfig.profileDirectory, ShUpConfig.PROFILE_PROPERTIES);
		loadPropertiesFromFile(ShUpConfig.profileProperties, profilePropertiesFile);

		// put settings into ShUpOnloadConfig for sh-ng
		ShUpOnloadConfig.setShanoirNg(Boolean.parseBoolean(ShUpConfig.profileProperties.getProperty("is.ng.up")));
		File keycloakJson = new File(ShUpConfig.profileDirectory, ShUpConfig.KEYCLOAK_JSON);
		if (keycloakJson.exists()) {
			ShUpConfig.keycloakJson = keycloakJson;
			System.out.println("keycloak.json successfully initialized.");
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
	
	/**
	 * This method download Dataset corresponding to the properties set by the
	 * user.
	 */
	private void download() {
		
		keycloakAuthentification();
		
		shanoirUploaderServiceClientNG = new ShanoirUploaderServiceClientNG();
		
		ShUpConfig.profileProperties.setProperty("shanoir.server.url", "http://" + getHost() + ":" + getPort());
		
		try {
			HttpResponse  response = shanoirUploaderServiceClientNG.downloadDatasetById(datasetId, formatId == 6 ? "dcm" : "nii");

			Header header = response.getFirstHeader(HttpHeaders.CONTENT_DISPOSITION);
			String fileName = header.getValue();
			fileName = fileName.replace("attachment;filename=", "");
			
			final File downloadedFile = new File(destDir + "/" + fileName);

	        FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
	        
		} catch (Exception e) {
			e.printStackTrace();
			exit("Download failed: " + e.getMessage());
		}
	}

	/**
	 * Gets the dataset id.
	 *
	 * @return the datasetId
	 */
	public long getDatasetId() {
		return datasetId;
	}

	/**
	 * Gets the dest dir.
	 *
	 * @return the destDir
	 */
	public File getDestDir() {
		return destDir;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.toolkit.ShanoirTkCLI#postParse()
	 */
	@Override
	protected void postParse() throws MissingArgumentException,
			DatatypeConfigurationException {
		
		if (cl.hasOption("datasetId")) {
			this.datasetId = Long.parseLong(cl.getOptionValue("datasetId"));
		}
		if (cl.hasOption("formatId")) {
			formatId = Long.parseLong(cl.getOptionValue("formatId"));
		}
		if (cl.hasOption("destDir")) {
			this.setDestDir(cl.getOptionValue("destDir"));
		}

	}

	/**
	 * Sets the dataset id.
	 *
	 * @param datasetId
	 *            the datasetId to set
	 */
	public void setDatasetId(final long datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * Sets the dest dir.
	 *
	 * @param destDir
	 *            the destDir to set
	 */
	public void setDestDir(final File destDir) {
		this.destDir = destDir;
	}

	/**
	 * Sets the dest dir.
	 *
	 * @param destDir
	 *            the new dest dir
	 */
	public void setDestDir(final String destDir) {
		this.destDir = new File(destDir);
		if (!this.destDir.isDirectory()) {
			throw new IllegalArgumentException(
					"Destination is not a directory! destDir: " + destDir);
		}
	}

}