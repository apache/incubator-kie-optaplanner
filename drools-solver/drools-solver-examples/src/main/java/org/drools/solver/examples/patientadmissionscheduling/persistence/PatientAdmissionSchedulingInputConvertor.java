package org.drools.solver.examples.patientadmissionscheduling.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.drools.solver.examples.common.persistence.AbstractInputConvertor;
import org.drools.solver.examples.patientadmissionscheduling.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.patientadmissionscheduling.domain.Specialism;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionSchedulingInputConvertor extends AbstractInputConvertor {

    public static void main(String[] args) {
        new PatientAdmissionSchedulingInputConvertor().convert();
    }

    protected String getExampleDirName() {
        return "patientadmissionscheduling";
    }

    public Solution readSolution(BufferedReader bufferedReader) throws IOException {
        PatientAdmissionSchedule patientAdmissionSchedule = new PatientAdmissionSchedule();
        patientAdmissionSchedule.setId(0L);

        readConstantLine(bufferedReader, "ARTICLE BENCHMARK DATA SET");
        int roomListSize = readIntegerValue(bufferedReader, "Rooms:");
        int roomPropertyListSize = readIntegerValue(bufferedReader, "Roomproperties:");
        int bedListSize = readIntegerValue(bufferedReader, "Beds:");
        int departmentListSize = readIntegerValue(bufferedReader, "Departments:");
        int specialismListSize = readIntegerValue(bufferedReader, "Specialisms:");
        int patientListSize = readIntegerValue(bufferedReader, "Patients:");
        int nightListSize = readIntegerValue(bufferedReader, "Planning horizon:");
        readEmptyLine(bufferedReader);
        readEmptyLine(bufferedReader);

        readConstantLine(bufferedReader, "SPECIALISMS:");
        List<Specialism> specialismList = new ArrayList<Specialism>(specialismListSize);
        for (int i = 0; i < specialismListSize; i++) {
            Specialism specialism = new Specialism();

            String line = bufferedReader.readLine();
            String[] lineTokens = line.split("\\ ");
            if (lineTokens.length != 2) {
                throw new IllegalArgumentException("Read line (" + line
                        + ") is expected to contain 2 tokens.");
            }
            specialism.setId(Long.parseLong(lineTokens[0]));
            specialism.setName(lineTokens[1]);
            specialismList.add(specialism);
        }
        patientAdmissionSchedule.setSpecialismList(specialismList);
        readEmptyLine(bufferedReader);

        // TODO

        
        return patientAdmissionSchedule;
    }

}