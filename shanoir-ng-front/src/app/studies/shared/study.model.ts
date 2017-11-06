import { StudyStatus } from "./enum/StudyStatus";
import { StudyType } from "./enum/StudyType";

export class Study {
	clinical: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    examinationIds: number[];
    id: number;
    monoCenter;
    name: string;
    protocolFilePathList: string[];
    startDate: Date;
    studyStatus: StudyStatus;
    studyType: StudyType;
    subjectNames: string[];
    visibleByDefault: boolean;
    withExamination: boolean;
}