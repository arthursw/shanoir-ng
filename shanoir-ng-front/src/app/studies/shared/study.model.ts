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

import { Entity } from '../../shared/components/entity/entity.abstract';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { User } from '../../users/shared/user.model';
import { ServiceLocator } from '../../utils/locator.service';
import { MembersCategory } from './members-category.model';
import { StudyCenter } from './study-center.model';
import { StudyType } from './study-type.enum';
import { StudyUser } from './study-user.model';
import { StudyService } from './study.service';
import { Timepoint } from './timepoint.model';

export class Study extends Entity {
    clinical: boolean;
    compatible: boolean = false;
    downloadableByDefault: boolean;
    endDate: Date;
    experimentalGroupsOfSubjects: IdNameObject[];
    id: number;
    membersCategories: MembersCategory[];
    monoCenter: boolean;
    name: string;
    nbExaminations: number;
    nbSujects: number;
    protocolFilePathList: string[];
    startDate: Date;
    studyCenterList: StudyCenter[] = [];
    studyStatus: 'IN_PROGRESS' | 'FINISHED'  = 'IN_PROGRESS';
    studyType: StudyType;
    subjectStudyList: SubjectStudy[] = [];
    studyUserList: StudyUser[] = [];
    timepoints: Timepoint[];
    visibleByDefault: boolean;
    withExamination: boolean;
    selected: boolean = false;
    
    private completeMembers(users: User[]) {
        return Study.completeMembers(this, users);
    }
    
    public static completeMembers(study: Study, users: User[]) {
        if (!study.studyUserList) return;
        for (let studyUser of study.studyUserList) {
            StudyUser.completeMember(studyUser, users); 
        }
    }

    protected getIgnoreList(): string[] {
        return super.getIgnoreList().concat(['completeMembers']);
    }
    
    service: StudyService = ServiceLocator.injector.get(StudyService);
}