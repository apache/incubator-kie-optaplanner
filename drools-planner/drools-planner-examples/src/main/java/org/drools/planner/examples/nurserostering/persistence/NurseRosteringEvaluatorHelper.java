package org.drools.planner.examples.nurserostering.persistence;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.planner.examples.common.business.SolutionBusiness;
import org.drools.planner.examples.nurserostering.app.NurseRosteringApp;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringEvaluatorHelper {

    private static final String INPUT_FILE_PREFIX = "long01";
    private static final String OUTPUT_FILE_SUFFIX = "_tmp";

    public static void main(String[] args) {
        NurseRosteringApp nurseRosteringApp = new NurseRosteringApp();
        SolutionBusiness solutionBusiness = nurseRosteringApp.createSolutionBusiness();
        Process process = null;
        try {
            File inputFile = new File(solutionBusiness.getImportDataDir(),
                    INPUT_FILE_PREFIX + ".xml").getCanonicalFile();
            File outputFile = new File(solutionBusiness.getExportDataDir(),
                    INPUT_FILE_PREFIX + OUTPUT_FILE_SUFFIX + ".xml").getCanonicalFile();
            File evaluatorDir = new File("local/competition/nurserostering/");
            String command = "java -jar evaluator.jar " + inputFile.getAbsolutePath()
                    + " " + outputFile.getAbsolutePath();
            process = Runtime.getRuntime().exec(command, null, evaluatorDir);
            EvaluatorSummaryFilterOutputStream out = new EvaluatorSummaryFilterOutputStream();
            IOUtils.copy(process.getInputStream(), out);
            IOUtils.copy(process.getErrorStream(), System.err);
            out.writeResults();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static class EvaluatorSummaryFilterOutputStream extends FilterOutputStream {

        private static final String EXCESS_PATTERN = "excess = ";
        
        private StringBuilder lineBuffer = new StringBuilder(120);
        private Map<String, int[]> excessMap = new LinkedHashMap<String, int[]>();

        private EvaluatorSummaryFilterOutputStream() {
            super(System.out);
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            if (b == '\n') {
                processLine(lineBuffer.toString());
                lineBuffer.delete(0, lineBuffer.length());
            } else {
                lineBuffer.append((char) b);
            }
        }

        private void processLine(String line) {
            int excessIndex = line.indexOf(EXCESS_PATTERN);
            if (excessIndex >= 0) {
                String key = line.substring(0, excessIndex);
                int value = Integer.parseInt(line.substring(excessIndex).replaceAll("excess = (\\d+) .*", "$1"));
                int[] excess = excessMap.get(key);
                if (excess == null) {
                    excess = new int[]{0, value};
                    excessMap.put(key, excess);
                } else {
                    excess[0]++;
                    excess[1] += value;
                }
            }
        }

        public void writeResults() {
            System.out.println("EvaluatorHelper results");
            System.out.println("=======================");
            for (Map.Entry<String, int[]> entry : excessMap.entrySet()) {
                int[] excess = entry.getValue();
                System.out.println(entry.getKey() + " count = " + excess[0] + " total = " + excess[1]);
            }
        }
    }

}
