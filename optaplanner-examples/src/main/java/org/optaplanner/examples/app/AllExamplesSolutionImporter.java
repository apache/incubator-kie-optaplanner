/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.examples.app;

import org.optaplanner.examples.cloudbalancing.persistence.CloudBalancingGenerator;
import org.optaplanner.examples.common.app.LoggingMain;
import org.optaplanner.examples.conferencescheduling.persistence.ConferenceSchedulingGenerator;
import org.optaplanner.examples.curriculumcourse.persistence.CurriculumCourseImporter;
import org.optaplanner.examples.examination.persistence.ExaminationImporter;
import org.optaplanner.examples.flightcrewscheduling.persistence.FlightCrewSchedulingGenerator;
import org.optaplanner.examples.machinereassignment.persistence.MachineReassignmentImporter;
import org.optaplanner.examples.meetingscheduling.persistence.MeetingSchedulingGenerator;
import org.optaplanner.examples.nqueens.persistence.NQueensGenerator;
import org.optaplanner.examples.nurserostering.persistence.NurseRosteringImporter;
import org.optaplanner.examples.pas.persistence.PatientAdmissionScheduleImporter;
import org.optaplanner.examples.projectjobscheduling.persistence.ProjectJobSchedulingImporter;
import org.optaplanner.examples.taskassigning.persistence.TaskAssigningGenerator;
import org.optaplanner.examples.tennis.persistence.TennisGenerator;
import org.optaplanner.examples.travelingtournament.persistence.TravelingTournamentImporter;
import org.optaplanner.examples.tsp.persistence.TspImporter;
import org.optaplanner.examples.vehiclerouting.persistence.VehicleRoutingImporter;

public class AllExamplesSolutionImporter extends LoggingMain {

    public static void main(String[] args) {
        new AllExamplesSolutionImporter().importAll();
    }

    public void importAll() {
        CloudBalancingGenerator.main(new String[0]);
        ConferenceSchedulingGenerator.main(new String[0]);
        CurriculumCourseImporter.main(new String[0]);
        ExaminationImporter.main(new String[0]);
        FlightCrewSchedulingGenerator.main(new String[0]);
        MachineReassignmentImporter.main(new String[0]);
        MeetingSchedulingGenerator.main(new String[0]);
        NQueensGenerator.main(new String[0]);
        NurseRosteringImporter.main(new String[0]);
        PatientAdmissionScheduleImporter.main(new String[0]);
        ProjectJobSchedulingImporter.main(new String[0]);
        TaskAssigningGenerator.main(new String[0]);
        TennisGenerator.main(new String[0]);
        TravelingTournamentImporter.main(new String[0]);
        TspImporter.main(new String[0]);
        VehicleRoutingImporter.main(new String[0]);
    }

}
