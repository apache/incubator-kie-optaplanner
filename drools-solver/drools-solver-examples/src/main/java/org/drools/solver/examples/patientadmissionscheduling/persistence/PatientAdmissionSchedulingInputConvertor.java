package org.drools.solver.examples.patientadmissionscheduling.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.drools.solver.examples.common.persistence.AbstractInputConvertor;
import org.drools.solver.examples.patientadmissionscheduling.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.patientadmissionscheduling.domain.Specialism;
import org.drools.solver.examples.patientadmissionscheduling.domain.Department;
import org.drools.solver.examples.patientadmissionscheduling.domain.DepartmentSpecialism;
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

    private Map<Long, Specialism> specialismMap = null;

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

        readSpecialismList(bufferedReader, patientAdmissionSchedule, specialismListSize);

        readDepartmentListAndDepartmentSpecialismList(bufferedReader, patientAdmissionSchedule, departmentListSize);

        
        return patientAdmissionSchedule;
    }

    private void readSpecialismList(BufferedReader bufferedReader, PatientAdmissionSchedule patientAdmissionSchedule,
            int specialismListSize) throws IOException {
        readConstantLine(bufferedReader, "SPECIALISMS:");
        List<Specialism> specialismList = new ArrayList<Specialism>(specialismListSize);
        specialismMap = new HashMap<Long, Specialism>(specialismListSize);
        for (int i = 0; i < specialismListSize; i++) {
            String line = bufferedReader.readLine();
            String[] lineTokens = splitBySpace(line, 2);
            Specialism specialism = new Specialism();
            specialism.setId(Long.parseLong(lineTokens[0]));
            specialism.setName(lineTokens[1]);
            specialismList.add(specialism);
            specialismMap.put(specialism.getId(), specialism);
        }
        patientAdmissionSchedule.setSpecialismList(specialismList);
        readEmptyLine(bufferedReader);
    }

    private void readDepartmentListAndDepartmentSpecialismList(BufferedReader bufferedReader, PatientAdmissionSchedule patientAdmissionSchedule, int departmentListSize) throws IOException {
        readConstantLine(bufferedReader, "DEPARTMENTS:");
        List<Department> departmentList = new ArrayList<Department>(departmentListSize);
        List<DepartmentSpecialism> departmentSpecialismList = new ArrayList<DepartmentSpecialism>(
                departmentListSize * 5);
        for (int i = 0; i < departmentListSize; i++) {
            String line = bufferedReader.readLine();
            String[] lineTokens = splitByPipeline(line, 2);

            String[] departmentTokens = splitBySpace(lineTokens[0], 4);
            Department department = new Department();
            department.setId(Long.parseLong(departmentTokens[0]));
            department.setName(departmentTokens[1]);
            int minimumAge = Integer.parseInt(departmentTokens[2]);
            if (minimumAge != 0) {
                department.setMinimumAge(Integer.valueOf(minimumAge));
            }
            int maximumAge = Integer.parseInt(departmentTokens[3]);
            if (maximumAge != 0) {
                department.setMaximumAge(Integer.valueOf(maximumAge));
            }
            departmentList.add(department);

            String[] departmentSpecialismTokens = splitBySpace(lineTokens[1]);
            if (departmentSpecialismTokens.length % 2 != 0) {
                throw new IllegalArgumentException("Read line (" + line
                        + ") is expected to contain even number of tokens (" + departmentSpecialismTokens.length
                        + ") after pipeline (|) seperated by a space ( ).");
            }
            for (int j = 0; j < departmentSpecialismTokens.length; j += 2) {
                int priority = Integer.parseInt(departmentSpecialismTokens[j]);
                long specialismId = Long.parseLong(departmentSpecialismTokens[j + 1]);
                DepartmentSpecialism departmentSpecialism = new DepartmentSpecialism();
                departmentSpecialism.setId((long) j / 2);
                departmentSpecialism.setDepartment(department);
                departmentSpecialism.setSpecialism(specialismMap.get(specialismId));
                departmentSpecialism.setPriority(priority);
                departmentSpecialismList.add(departmentSpecialism);
            }
        }
        patientAdmissionSchedule.setDepartmentList(departmentList);
        patientAdmissionSchedule.setDepartmentSpecialismList(departmentSpecialismList);
        readEmptyLine(bufferedReader);
    }

    private String[] splitBySpace(String line) {
        String[] lineTokens = line.trim().split("\\ ");
        return lineTokens;
    }

    private String[] splitBySpace(String line, int numberOfTokens) {
        String[] lineTokens = line.trim().split("\\ ");
        if (lineTokens.length != numberOfTokens) {
            throw new IllegalArgumentException("Read line (" + line
                    + ") is expected to contain " +  numberOfTokens + " tokens seperated by a space ( ).");
        }
        return lineTokens;
    }

    private String[] splitByPipeline(String line, int numberOfTokens) {
        String[] lineTokens = line.trim().split("\\|");
        if (lineTokens.length != numberOfTokens) {
            throw new IllegalArgumentException("Read line (" + line
                    + ") is expected to contain " +  numberOfTokens + " tokens seperated by a pipeline (|).");
        }
        return lineTokens;
    }

}