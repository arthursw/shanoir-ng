/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.dicom.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.tool.findscu.FindSCU.InformationModel;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.op.CMove;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.param.DicomProgress;
import org.weasis.dicom.param.DicomState;
import org.weasis.dicom.param.ProgressListener;

@Service
public class QueryPACSService {
	
	private static final Logger LOG = LoggerFactory.getLogger(QueryPACSService.class);

	@Value("${shanoir.import.pacs.query.aet.calling.name}")
	private String callingName;

	@Value("${shanoir.import.pacs.query.aet.calling.host}")
	private String callingHost;
	
	@Value("${shanoir.import.pacs.query.aet.calling.port}")
	private Integer callingPort;
	
	@Value("${shanoir.import.pacs.query.aet.called.name}")
	private String calledName;

	@Value("${shanoir.import.pacs.query.aet.called.host}")
	private String calledHost;
	
	@Value("${shanoir.import.pacs.query.aet.called.port}")
	private Integer calledPort;
	
	private DicomNode calling;
	
	private DicomNode called;
	
	@Value("${shanoir.import.pacs.store.aet.called.name}")
	private String calledNameSCP;
	
	@PostConstruct
	private void initDicomNodes() {
		// Initialize connection configuration parameters here: to be used for all queries
		this.calling = new DicomNode(callingName, callingHost, callingPort);
		this.called = new DicomNode(calledName, calledHost, calledPort);
	}
	
	public ImportJob queryCFIND(DicomQuery dicomQuery) throws ShanoirImportException {
		ImportJob importJob = new ImportJob();
		importJob.setFromPacs(true);
		/**
		 * In case of any patient specific search field is filled, work on patient level. Highest priority.
		 */
		if (StringUtils.isNotEmpty(dicomQuery.getPatientName())
			|| StringUtils.isNotEmpty(dicomQuery.getPatientID())
			|| StringUtils.isNotEmpty(dicomQuery.getPatientBirthDate())) {
			// @Todo: implement wild card search
			// Do Fuzzy search on base of patient name here
//			if (StringUtils.isNotEmpty(dicomQuery.getPatientName())
//				&& (dicomQuery.getPatientName().contains("*") || !dicomQuery.getPatientName().contains("^"))) {
			queryPatientLevel(dicomQuery, calling, called, importJob);
			// Do precise search here, using name, id or date
//			} else {	
//			}
		/**
		 * In case of any study specific search field is filled, work on study level. Second priority.
		 */
		} else if (StringUtils.isNotEmpty(dicomQuery.getStudyDescription())
			|| StringUtils.isNotEmpty(dicomQuery.getStudyDate())) {
			queryStudyLevel(dicomQuery, calling, called, importJob);
		} else {
			throw new ShanoirImportException("DicomQuery: missing parameters.");
		}
		return importJob;
	}
	
	public void queryCMOVE(Serie serie) {
		DicomProgress progress = new DicomProgress();
		progress.addProgressListener(new ProgressListener() {
			@Override
			public void handleProgression(DicomProgress progress) {
				LOG.debug("Remaining operations:" + progress.getNumberOfRemainingSuboperations());
			}
		});
		DicomParam[] params = { new DicomParam(Tag.QueryRetrieveLevel, "SERIES"),
				new DicomParam(Tag.SeriesInstanceUID, serie.getSeriesInstanceUID()) };
		AdvancedParams options = new AdvancedParams();
		options.getQueryOptions().add(QueryOption.RELATIONAL); // Required for QueryRetrieveLevel other than study
		DicomState state = CMove.process(options, calling, called, calledNameSCP, progress, params);
	}

	/**
	 * This method queries on patient root level.
	 * @param dicomQuery
	 * @param calling
	 * @param called
	 * @param importJob
	 */
	private void queryPatientLevel(DicomQuery dicomQuery, DicomNode calling, DicomNode called, ImportJob importJob) {
		DicomParam patientName = initDicomParam(Tag.PatientName, dicomQuery.getPatientName());
		DicomParam patientID = initDicomParam(Tag.PatientID, dicomQuery.getPatientID());
		DicomParam patientBirthDate = initDicomParam(Tag.PatientBirthDate, dicomQuery.getPatientBirthDate());
		DicomParam[] params = { patientName, patientID, patientBirthDate, new DicomParam(Tag.PatientBirthName), new DicomParam(Tag.PatientSex) };
		List<Attributes> attributesPatients = queryCFIND(params, QueryRetrieveLevel.PATIENT, calling, called);
		if (attributesPatients != null) {
			List<Patient> patients = new ArrayList<Patient>();
			for (int i = 0; i < attributesPatients.size(); i++) {
				Patient patient = new Patient(attributesPatients.get(i));
				patients.add(patient);
				queryStudies(calling, called, patient);
			}
			importJob.setPatients(patients);
		}
	}

	/**
	 * This method queries on study root level.
	 * @param dicomQuery
	 * @param calling
	 * @param called
	 * @param importJob
	 */
	private void queryStudyLevel(DicomQuery dicomQuery, DicomNode calling, DicomNode called, ImportJob importJob) {
		DicomParam studyDescription = initDicomParam(Tag.StudyDescription, dicomQuery.getStudyDescription());
		DicomParam studyDate = initDicomParam(Tag.StudyDate, dicomQuery.getStudyDate());
		DicomParam[] params = { studyDescription, studyDate, new DicomParam(Tag.PatientName),
			new DicomParam(Tag.PatientID), new DicomParam(Tag.PatientBirthDate), new DicomParam(Tag.PatientBirthName),
			new DicomParam(Tag.PatientSex), new DicomParam(Tag.StudyInstanceUID) };
		List<Attributes> attributesStudies = queryCFIND(params, QueryRetrieveLevel.STUDY, calling, called);
		if (attributesStudies != null) {
			List<Patient> patients = new ArrayList<Patient>();
			for (int i = 0; i < attributesStudies.size(); i++) {
				// handle patients
				Patient patient = new Patient(attributesStudies.get(i));
				patient = addPatientIfNotExisting(patients, patient);
				// handle studies
				Study study = new Study(attributesStudies.get(i));
				patient.getStudies().add(study);
				querySeries(calling, called, study);
			}
			importJob.setPatients(patients);
		}
	}

	/**
	 * This method adds a new patient to the patients list, if not already existing.
	 * @param patients
	 * @param newPatient
	 */
	private Patient addPatientIfNotExisting(List<Patient> patients, Patient newPatient) {
		for (Iterator iterator = patients.iterator(); iterator.hasNext();) {
			Patient patientInList = (Patient) iterator.next();
			if (patientInList.getPatientID().equals(newPatient.getPatientID())) {
				return patientInList;
			}
		}
		newPatient.setStudies(new ArrayList<Study>());
		patients.add(newPatient);
		return newPatient;
	}
	
	/**
	 * This method returns a created DicomParam given tag and value.
	 * @param tag
	 * @param value
	 * @return
	 */
	private DicomParam initDicomParam(int tag, String value) {
		DicomParam dicomParam;
		if (StringUtils.isNotEmpty(value)) {
			dicomParam = new DicomParam(tag, value);
		} else {
			dicomParam = new DicomParam(tag);
		}
		return dicomParam;
	}

	/**
	 * This method queries for studies, creates studies and adds them to patients.
	 * @param calling
	 * @param called
	 * @param patient
	 */
	private void queryStudies(DicomNode calling, DicomNode called, Patient patient) {
		DicomParam[] params = { new DicomParam(Tag.PatientID, patient.getPatientID()),
				new DicomParam(Tag.StudyInstanceUID), new DicomParam(Tag.StudyDate), new DicomParam(Tag.StudyDescription)};
		List<Attributes> attributesStudies = queryCFIND(params, QueryRetrieveLevel.STUDY, calling, called);
		if (attributesStudies != null) {
			List<Study> studies = new ArrayList<Study>();
			for (int i = 0; i < attributesStudies.size(); i++) {
				Study study = new Study(attributesStudies.get(i));
				studies.add(study);
				querySeries(calling, called, study);
			}
			patient.setStudies(studies);
		}
	}

	/**
	 * This method queries for series, creates them and adds them to studies.
	 * @param calling
	 * @param called
	 * @param study
	 */
	private void querySeries(DicomNode calling, DicomNode called, Study study) {
		DicomParam[] params = {
			new DicomParam(Tag.StudyInstanceUID, study.getStudyInstanceUID()),
			new DicomParam(Tag.SeriesInstanceUID),
			new DicomParam(Tag.SeriesDescription),
			new DicomParam(Tag.SeriesDate),
			new DicomParam(Tag.SeriesNumber),
			new DicomParam(Tag.Modality),
			new DicomParam(Tag.ProtocolName),
			new DicomParam(Tag.Manufacturer),
			new DicomParam(Tag.ManufacturerModelName),
			new DicomParam(Tag.DeviceSerialNumber)	
		};
		List<Attributes> attributes = queryCFIND(params, QueryRetrieveLevel.SERIES, calling, called);
		if (attributes != null) {
			List<Serie> series = new ArrayList<Serie>();
			for (int i = 0; i < attributes.size(); i++) {
				Serie serie = new Serie(attributes.get(i));
				if (serie.getModality() != null && !"PR".equals(serie.getModality()) && !"SR".equals(serie.getModality())) {
					queryInstances(calling, called, serie, study);
					if (!serie.getInstances().isEmpty()) {
						series.add(serie);						
					} else {
						LOG.warn("Serie found with empty instances and therefore ignored (SerieInstanceUID: " + serie.getSeriesInstanceUID() + ").");
					}
				} else {
					LOG.warn("Serie found with wrong modality (PR or SR) therefore ignored (SerieInstanceUID: " + serie.getSeriesInstanceUID() + ").");					
				}
			}
			study.setSeries(series);
		}
	}
	
	/**
	 * This method queries for instances/images, creates them and adds them to series.
	 * 
	 * @param calling
	 * @param called
	 * @param serie
	 */
	private void queryInstances(DicomNode calling, DicomNode called, Serie serie, Study study) {
		DicomParam[] params = {
			new DicomParam(Tag.StudyInstanceUID, study.getStudyInstanceUID()),
			new DicomParam(Tag.SeriesInstanceUID, serie.getSeriesInstanceUID()),
			new DicomParam(Tag.SOPInstanceUID),
			new DicomParam(Tag.InstanceNumber)	
		};
		List<Attributes> attributes = queryCFIND(params, QueryRetrieveLevel.IMAGE, calling, called);
		if (attributes != null) {
			List<Instance> instances = new ArrayList<Instance>();
			for (int i = 0; i < attributes.size(); i++) {
				Instance instance = new Instance(attributes.get(i));
				instances.add(instance);
			}
			serie.setInstances(instances);
		}
	}
	
	/**
	 * This method does a C-FIND query and returns the results.
	 * @param params
	 * @param level
	 * @param calling
	 * @param called
	 * @return
	 */
	private List<Attributes> queryCFIND(DicomParam[] params, QueryRetrieveLevel level, final DicomNode calling, final DicomNode called) {
		AdvancedParams options = new AdvancedParams();
		if (level.equals(QueryRetrieveLevel.PATIENT)) {
			options.setInformationModel(InformationModel.PatientRoot);
		} else if (level.equals(QueryRetrieveLevel.STUDY)) {
			options.setInformationModel(InformationModel.StudyRoot);
		} else if (level.equals(QueryRetrieveLevel.SERIES)) {
			options.setInformationModel(InformationModel.StudyRoot);
		} else if (level.equals(QueryRetrieveLevel.IMAGE)) {
			options.setInformationModel(InformationModel.StudyRoot);
		}
		logQuery(params, options);
		DicomState state = CFind.process(options, calling, called, 0, level, params);
		return state.getDicomRSP();
	}

	/**
	 * This method logs the params and options of the PACS query.
	 * @param params
	 * @param options
	 */
	private void logQuery(DicomParam[] params, AdvancedParams options) {
		LOG.info("Calling PACS, C-FIND with level: " + options.getInformationModel().toString() + " and params:");
		for (int i = 0; i < params.length; i++) {
			LOG.info("Tag: " + params[i].getTagName() + ", Value: " + Arrays.toString(params[i].getValues())); 
		}
	}

}
