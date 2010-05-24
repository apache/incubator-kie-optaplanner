package org.drools.planner.examples.nurserostering.persistence;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.drools.planner.examples.common.business.SolutionBusiness;
import org.drools.planner.examples.nurserostering.app.NurseRosteringApp;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringEvaluatorHelper {

    private static final String INPUT_FILE_PREFIX = "long_late04";
    private static final String OUTPUT_FILE_SUFFIX = "_feasibleInitialized";
    private static final String DEFAULT_LINE_CONTAINS_FILTER = null;

    public static void main(String[] args) {
        String lineContainsFilter;
        if (args.length > 0) {
            lineContainsFilter = args[0];
        } else {
            lineContainsFilter = DEFAULT_LINE_CONTAINS_FILTER;
        }
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
            EvaluatorSummaryFilterOutputStream out = new EvaluatorSummaryFilterOutputStream(outputFile.getName(), lineContainsFilter);
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

    private static class EvaluatorSummaryFilterOutputStream extends OutputStream {

        private String name;
        private String lineContainsFilter;

        private StringBuilder lineBuffer = new StringBuilder(120);
        private Map<String, int[]> costMap = new TreeMap<String, int[]>();
        private String lastEmployeeCode = null;

        private EvaluatorSummaryFilterOutputStream(String name, String lineContainsFilter) {
            super();
            this.name = name;
            this.lineContainsFilter = lineContainsFilter;
        }

        public void write(int c) throws IOException {
            if (c == '\n') {
                String line = lineBuffer.toString();
                lineBuffer.delete(0, lineBuffer.length());
                processLine(line);
            } else {
                lineBuffer.append((char) c);
            }
        }

        private void processLine(String line) {
            int employeeIndex = line.indexOf("Employee: ");
            if (employeeIndex >= 0) {
                lastEmployeeCode = line.substring(employeeIndex).replaceAll("Employee: (.+)", "$1");
            } else if (line.contains("Penalty:")) {
                lastEmployeeCode = null;
            }
            if (lineContainsFilter == null || line.contains(lineContainsFilter)) {
                int excessIndex = line.indexOf("excess = ");
                if (excessIndex >= 0) {
                    String key = line.substring(0, excessIndex);
                    int costIndex = line.indexOf("cost = ");
                    int value = Integer.parseInt((line.substring(costIndex) + " ").replaceAll("cost = (\\d+) .*", "$1"));
                    int[] cost = costMap.get(key);
                    if (cost == null) {
                        cost = new int[]{1, value};
                        costMap.put(key, cost);
                    } else {
                        cost[0]++;
                        cost[1] += value;
                    }
                }
                if (lastEmployeeCode != null) {
                    System.out.print("E(" + lastEmployeeCode + ")  ");
                }
                System.out.println(line);
            }
        }

        public void writeResults() {
            System.out.println("EvaluatorHelper results for " + name);
            int grandTotal = 0;
            if (lineContainsFilter != null) {
                System.out.println("with lineContainsFilter (" + lineContainsFilter + ")");
            }
            for (Map.Entry<String, int[]> entry : costMap.entrySet()) {
                int[] cost = entry.getValue();
                grandTotal += cost[1];
                System.out.println(entry.getKey() + " count = " + cost[0] + " total = " + cost[1]);
            }
            System.out.println("Grand total: " + grandTotal);
        }
    }

}
