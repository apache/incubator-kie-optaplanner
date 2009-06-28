package org.drools.solver.examples.patientadmissionschedule.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.patientadmissionschedule.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.patientadmissionschedule.domain.Patient;
import org.drools.solver.examples.patientadmissionschedule.domain.AdmissionPart;
import org.drools.solver.examples.patientadmissionschedule.domain.BedDesignation;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleOutputConvertor extends LoggingMain {

    private static final String INPUT_FILE_SUFFIX = ".xml";
    private static final String OUTPUT_FILE_SUFFIX = ".txt";

    public static void main(String[] args) {
        new PatientAdmissionScheduleOutputConvertor().convert();
    }

    private final File inputDir = new File("data/patientadmissionschedule/solved/");
    private final File outputDir = new File("data/patientadmissionschedule/output/");

    public void convert() {
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-solver-examples and contain: " + inputDir);
        }
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith(INPUT_FILE_SUFFIX)) {
                PatientAdmissionSchedule patientAdmissionSchedule = (PatientAdmissionSchedule) solutionDao.readSolution(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length())
                        + OUTPUT_FILE_SUFFIX;
                File outputFile = new File(outputDir, outputFileName);
                writePatientAdmissionSchedule(patientAdmissionSchedule, outputFile);
            }
        }
    }

    public void writePatientAdmissionSchedule(PatientAdmissionSchedule patientAdmissionSchedule, File outputFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            writePatientAdmissionSchedule(patientAdmissionSchedule, bufferedWriter);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }

    public void writePatientAdmissionSchedule(PatientAdmissionSchedule patientAdmissionSchedule, BufferedWriter bufferedWriter) throws IOException {
        Collections.sort(patientAdmissionSchedule.getBedDesignationList());
        for (Patient patient : patientAdmissionSchedule.getPatientList()) {
            bufferedWriter.write(Long.toString(patient.getId()));
            for (BedDesignation bedDesignation : patientAdmissionSchedule.getBedDesignationList()) {
                if (bedDesignation.getPatient().equals(patient)) {
                    bufferedWriter.write(" " + Long.toString(bedDesignation.getBed().getId()));
                }
            }
            bufferedWriter.write("\n");
        }
    }

}