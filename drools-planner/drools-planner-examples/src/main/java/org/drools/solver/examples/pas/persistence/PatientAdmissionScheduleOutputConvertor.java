package org.drools.solver.examples.pas.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.common.persistence.AbstractOutputConvertor;
import org.drools.solver.examples.pas.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.pas.domain.Patient;
import org.drools.solver.examples.pas.domain.BedDesignation;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleOutputConvertor extends AbstractOutputConvertor {

    public static void main(String[] args) {
        new PatientAdmissionScheduleOutputConvertor().convertAll();
    }

    protected String getExampleDirName() {
        return "pas";
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