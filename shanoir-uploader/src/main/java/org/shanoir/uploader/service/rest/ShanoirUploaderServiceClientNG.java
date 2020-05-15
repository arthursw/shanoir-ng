package org.shanoir.uploader.service.rest;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.utils.Util;
import org.springframework.core.io.ByteArrayResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * Service layer for REST services of sh-ng.
 *
 * @author mkain
 *
 */
public class ShanoirUploaderServiceClientNG {

	private static Logger logger = Logger.getLogger(ShanoirUploaderServiceClientNG.class);
	
	private static final String SHANOIR_SERVER_URL = "shanoir.server.url";

	private static final String SERVICE_STUDIES_NAMES_CENTERS = "service.studies.names.centers";

	private static final String SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS = "service.studycards.find.by.study.ids";

	private static final String SERVICE_ACQUISITION_EQUIPMENT_BY_ID = "service.acquisition.equipment.find.by.id";
	
	private static final String SERVICE_SUBJECTS_FIND_BY_IDENTIFIER = "service.subjects.find.by.identifier";

	private static final String SERVICE_DATASETS = "service.datasets";
	
	private static final String SERVICE_SUBJECTS_CREATE = "service.subjects.create";
	
	private static final String SERVICE_EXAMINATIONS_CREATE = "service.examinations.create";
	
	private static final String SERVICE_IMPORTER_CREATE_TEMP_DIR = "service.importer.create.temp.dir";
	
	private static final String SERVICE_IMPORTER_START_IMPORT_JOB = "service.importer.start.import.job";

	private static final String SERVICE_IMPORTER_START_IMPORT = "service.importer.start.import";

	private static final String SERVICE_EXAMINATIONS_BY_SUBJECT_ID = "service.examinations.find.by.subject.id";

	private static final String SERVICE_SUBJECTS_BY_STUDY_ID = "service.subjects.by.study.id";

	private HttpService httpService;
	
	private String serverURL;

	private String serviceURLStudiesNamesAndCenters;
	
	private String serviceURLStudyCardsByStudyIds;
	
	private String serviceURLAcquisitionEquipmentById;
	
	private String serviceURLSubjectsFindByIdentifier;
	
	private String serviceURLDatasets;
	
	private String serviceURLSubjectsCreate;
	
	private String serviceURLExaminationsCreate;
	
	private String serviceURLImporterCreateTempDir;
	
	private String serviceURLImporterStartImportJob;

	private String serviceURLImporterStartImport;

	private String serviceURLExaminationsBySubjectId;

	private String serviceURLSubjectsByStudyId;

	public ShanoirUploaderServiceClientNG() {
		this.httpService = new HttpService();
		this.serverURL = ShUpConfig.profileProperties.getProperty(SHANOIR_SERVER_URL);
			this.serviceURLStudiesNamesAndCenters = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_STUDIES_NAMES_CENTERS);
		this.serviceURLStudyCardsByStudyIds = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS);
		this.serviceURLAcquisitionEquipmentById = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_ACQUISITION_EQUIPMENT_BY_ID);
		this.serviceURLSubjectsFindByIdentifier = this.serverURL
			+ ShUpConfig.profileProperties.getProperty(SERVICE_SUBJECTS_FIND_BY_IDENTIFIER);
		this.serviceURLDatasets = this.serverURL
			+ ShUpConfig.profileProperties.getProperty(SERVICE_DATASETS);
		this.serviceURLSubjectsCreate = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_SUBJECTS_CREATE);
		this.serviceURLExaminationsCreate = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_EXAMINATIONS_CREATE);
		this.serviceURLImporterCreateTempDir = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_IMPORTER_CREATE_TEMP_DIR);
		this.serviceURLImporterStartImportJob = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_IMPORTER_START_IMPORT_JOB);
		this.serviceURLExaminationsBySubjectId = this.serverURL
		+ ShUpConfig.profileProperties.getProperty(SERVICE_EXAMINATIONS_BY_SUBJECT_ID);
		this.serviceURLSubjectsByStudyId = this.serverURL
		+ ShUpConfig.profileProperties.getProperty(SERVICE_SUBJECTS_BY_STUDY_ID);
		this.serviceURLImporterStartImport = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_IMPORTER_START_IMPORT);
		logger.info("ShanoirUploaderServiceNG successfully initialized.");
	}
	
	public List<Study> findStudiesNamesAndCenters() {
		HttpResponse response = httpService.get(this.serviceURLStudiesNamesAndCenters);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
			List<Study> studies = Util.getMappedList(response, Study.class);
			return studies;
		} else {
			return null;
		}
	}

	public List<StudyCard> findStudyCardsByStudyIds(IdList studyIds) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(studyIds);
			HttpResponse response = httpService.post(this.serviceURLStudyCardsByStudyIds, json);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				List<StudyCard> studyCards = Util.getMappedList(response, StudyCard.class);
				return studyCards;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Subject findSubjectBySubjectIdentifier(String subjectIdentifier) throws Exception {
		HttpResponse response = httpService.get(this.serviceURLSubjectsFindByIdentifier + subjectIdentifier);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
			Subject subjectDTO = Util.getMappedObject(response, Subject.class);
			return subjectDTO;
		} else {
			return null;
		}
	}
	
	public String createTempDir() throws Exception {
		HttpResponse response = httpService.get(this.serviceURLImporterCreateTempDir);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
			String importTempDirId = Util.getMappedObject(response, String.class);
			return importTempDirId;
		} else {
			return null;
		}
	}
		
	public List<Examination> findExaminationsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			HttpResponse response = httpService.get(this.serviceURLExaminationsBySubjectId + subjectId);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				List<Examination> examinations = Util.getMappedList(response, Examination.class);
				return examinations;
			}
		}
		return null;
	}

	public List<Long> findDatasetIdsByStudyId(Long studyId) throws Exception {
		if (studyId != null) {
			List<Long> datasetIds = new ArrayList<Long>();
			HttpResponse response = httpService.get(this.serviceURLSubjectsByStudyId + studyId + "/allSubjects");
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				List<Subject> subjects = Util.getMappedList(response, Subject.class);
				for(Subject subject : subjects) {
					List<Long> ids = findDatasetIdsBySubjectIdStudyId(subject.getId(), studyId);
					if(ids != null) {
						datasetIds.addAll(ids);
					}
				}
				return datasetIds;
			}
		}
		return null;
	}

	public List<Long> findDatasetIdsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			HttpResponse response = httpService.get(this.serviceURLDatasets + "subject/" + subjectId);

			String responseEntityString = EntityUtils.toString(response.getEntity());
			System.out.println(responseEntityString);
		
			List<Header> httpHeaders = Arrays.asList(response.getAllHeaders());        
			for (Header header : httpHeaders) {
				System.out.println("Headers.. name,value:"+header.getName() + "," + header.getValue());
			}
			System.out.println(response.getStatusLine());
			
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				List<Long> datasetIds = Util.getMappedList(response, Long.class);
				return datasetIds;
			}
		}
		return null;
	}

	public List<Long> findDatasetIdsBySubjectIdStudyId(Long subjectId, Long studyId) throws Exception {
		if (subjectId != null) {
			
			HttpResponse response = httpService.get(this.serviceURLDatasets + "subject/" + subjectId + "/study/" + studyId);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				List<Long> datasetIds = Util.getMappedList(response, Long.class);
				return datasetIds;
			}
		}
		return null;
	}

	public AcquisitionEquipment findAcquisitionEquipmentById(Long acquisitionEquipmentId) throws Exception {
		if (acquisitionEquipmentId != null) {
			HttpResponse response = httpService.get(this.serviceURLAcquisitionEquipmentById + acquisitionEquipmentId);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				AcquisitionEquipment acquisitionEquipment = Util.getMappedObject(response, AcquisitionEquipment.class);
				return acquisitionEquipment;
			}
		}
		return null;
	}
	
	public void uploadFile(String tempDirId, File file) throws Exception {
		HttpResponse response = httpService.postFile(this.serviceURLImporterCreateTempDir, tempDirId, file);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
		} else {
			throw new Exception("Error in uploadFile.");
		}
	}
	
	public void startImportJob(String importJobJsonStr) throws Exception {
		HttpResponse response = httpService.post(this.serviceURLImporterStartImportJob, importJobJsonStr);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
		} else {
			throw new Exception("Error in startImportJob.");
		}
	}
	
	public void startImport(String exchangeJsonStr) throws Exception {
		HttpResponse response = httpService.post(this.serviceURLImporterStartImport, exchangeJsonStr);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
		} else {
			throw new Exception("Error in startImport.");
		}
	}

	public HttpResponse downloadDatasetById(Long datasetId, String format) throws Exception {
		if (datasetId != null) {
			URI url = UriBuilder.fromUri(this.serviceURLDatasets + "download/" + datasetId).queryParam("format", format).build();
			
			HttpResponse response = httpService.get(url.toString());

			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				return response;
			}
		}
		return null;
	}
	
	/**
	 * This method creates a subject on the server.
	 * 
	 * @param studyId
	 * @param studyCardId
	 * @param modeSubjectCommonName
	 * @param subject
	 * @return boolean true, if success
	 */
	public Subject createSubject(
			final Subject subject,
			final boolean modeSubjectCommonNameManual,
			final Long centerId) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(subject);
			HttpResponse response;
			if (modeSubjectCommonNameManual) {
				response = httpService.post(this.serviceURLSubjectsCreate, json);
			} else {
				response = httpService.post(this.serviceURLSubjectsCreate + "?centerId=" + centerId, json);
			}
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				Subject subjectDTOCreated = Util.getMappedObject(response, Subject.class);
				return subjectDTOCreated;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * This method updates a subject on the server and therefore updates
	 * the rel_subject_study list too.
	 * 
	 * @param subject
	 * @return
	 */
	public Subject createSubjectStudy(
			final Subject subject) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(subject);
			HttpResponse response = httpService.put(this.serviceURLSubjectsCreate + "/" + subject.getId(), json);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				Subject subjectCreated = Util.getMappedObject(response, Subject.class);
				return subjectCreated;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * This method creates an examination on the server.
	 * 
	 * @param examinationDTO
	 * @return
	 */
	public Examination createExamination(final Examination examinationDTO) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(examinationDTO);
			HttpResponse response = httpService.post(this.serviceURLExaminationsCreate, json);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				Examination examinationDTOCreated = Util.getMappedObject(response, Examination.class);
				return examinationDTOCreated;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}