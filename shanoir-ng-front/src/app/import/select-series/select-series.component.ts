import { Component, Output, EventEmitter, Input, SimpleChanges, OnChanges } from '@angular/core';
import { PatientDicom, SerieDicom, EquipmentDicom } from "./../dicom-data.model";
import { Study } from '../../studies/shared/study.model';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { AbstractImportStepComponent } from '../import-step.abstract';
import { slideDown } from '../../shared/animations/animations';

@Component({
    selector: 'select-series',
    templateUrl: 'select-series.component.html',
    styleUrls: ['select-series.component.css', '../import.step.css'],
    animations: [slideDown]
})
export class SelectSeriesComponent extends AbstractImportStepComponent implements OnChanges {

    @Input() patients: PatientDicom[];
    @Input() dataFiles: any;
    @Output() patientsChange = new EventEmitter<PatientDicom[]>();
    private detailedPatient: Object;
    private detailedSerie: Object;
    private papayaParams: object[];

    constructor() {
        super();
    }

    private showSerieDetails(nodeParams: any): void {
        this.detailedPatient = null;
        if (nodeParams && this.detailedSerie && nodeParams.seriesInstanceUID == this.detailedSerie["seriesInstanceUID"]) {
            this.detailedSerie = null;
        } else {
            this.detailedSerie = nodeParams;
        }
    }

    private showPatientDetails(nodeParams: any): void {
        this.detailedSerie = null;
        if (nodeParams && this.detailedPatient && nodeParams.patientID == this.detailedPatient["patientID"]) {
            this.detailedPatient = null;
        } else {
            this.detailedPatient = nodeParams;
        }
    }

    private onPatientUpdate(): void {
        this.updateValidity();
        this.patientsChange.emit(this.patients);
    }

    private initPapaya(serie: SerieDicom): void {
        let that = this;
        let entries = serie.images.map(function(name) {
            return that.dataFiles.files[name.path];
        });
        let listOfPromises = entries.map(function(a) {
            return a.async("arraybuffer");
        });

        let promiseOfList = Promise.all(listOfPromises);

        promiseOfList.then(function (values) {
            let params: object[] = [];
            params['binaryImages'] = [values];
            that.papayaParams = params;
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['patients']) {
            this.patientsChange.emit(this.patients);
        }
        this.updateValidity();
    }

    getValidity(): boolean {
        if (!this.patients) return false;
        for (let patient of this.patients) {
            for (let study of patient.studies) {
                for (let serie of study.series) {
                    if (serie.selected) return true;
                }
            }
        }
        return false;
    }
}