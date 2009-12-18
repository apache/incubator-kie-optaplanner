package org.drools.planner.examples.pas.persistence;

import java.io.IOException;
import java.util.Collections;

import org.drools.planner.examples.common.persistence.AbstractOutputConvertor;
import org.drools.planner.examples.pas.domain.PatientAdmissionSchedule;
import org.drools.planner.examples.pas.domain.Patient;
import org.drools.planner.examples.pas.domain.BedDesignation;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleOutputConvertor extends AbstractOutputConvertor {

    public static void main(String[] args) {
        new PatientAdmissionScheduleOutputConvertor().convertAll();
    }

    public PatientAdmissionScheduleOutputConvertor() {
        super(new PatientAdmissionScheduleDaoImpl());
    }

    public OutputBuilder createOutputBuilder() {
        return new PatientAdmissionScheduleOutputBuilder();
    }

    public class PatientAdmissionScheduleOutputBuilder extends OutputBuilder {

        private PatientAdmissionSchedule patientAdmissionSchedule;

        public void setSolution(Solution solution) {
            patientAdmissionSchedule = (PatientAdmissionSchedule) solution;
        }

        public void writeSolution() throws IOException {
            Collections.sort(patientAdmissionSchedule.getBedDesignationList());
            for (Patient patient : patientAdmissionSchedule.getPatientList()) {
                bufferedWriter.write(Long.toString(patient.getId()));
                for (BedDesignation bedDesignation : patientAdmissionSchedule.getBedDesignationList()) {
                    if (bedDesignation.getPatient().equals(patient)) {
                        for (int i = 0; i < bedDesignation.getAdmissionPart().getNightCount(); i++) {
                            bufferedWriter.write(" " + Long.toString(bedDesignation.getBed().getId()));
                        }
                    }
                }
                bufferedWriter.write("\n");
            }
        }
    }

}