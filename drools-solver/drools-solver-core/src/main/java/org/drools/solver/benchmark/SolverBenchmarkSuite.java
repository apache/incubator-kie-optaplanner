package org.drools.solver.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.drools.solver.config.localsearch.LocalSearchSolverConfig;
import org.drools.solver.core.Solver;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.benchmark.statistic.BestScoreStatistic;
import org.drools.solver.benchmark.statistic.SolverStatistic;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("solverBenchmarkSuite")
public class SolverBenchmarkSuite {

    public static final NumberFormat TIME_FORMAT = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private SolvedSolutionVerbosity solvedSolutionVerbosity = null;
    private File solvedSolutionFilesDirectory = null;
    private boolean sortSolverBenchmarks = true;
    private Comparator<SolverBenchmark> solverBenchmarkComparator = null;
    private SolverStatisticType solverStatisticType = SolverStatisticType.NONE;
    private File solverStatisticFilesDirectory = null;

    @XStreamAlias("inheritedLocalSearchSolver")
    private LocalSearchSolverConfig inheritedLocalSearchSolverConfig = null;
    @XStreamImplicit(itemFieldName = "inheritedUnsolvedSolutionFile")
    private List<File> inheritedUnsolvedSolutionFileList = null;

    @XStreamImplicit(itemFieldName = "solverBenchmark")
    private List<SolverBenchmark> solverBenchmarkList = null;

    public SolvedSolutionVerbosity getSolvedSolutionVerbosity() {
        return solvedSolutionVerbosity;
    }

    public void setSolvedSolutionVerbosity(SolvedSolutionVerbosity solvedSolutionVerbosity) {
        this.solvedSolutionVerbosity = solvedSolutionVerbosity;
    }

    public File getSolvedSolutionFilesDirectory() {
        return solvedSolutionFilesDirectory;
    }

    public void setSolvedSolutionFilesDirectory(File solvedSolutionFilesDirectory) {
        this.solvedSolutionFilesDirectory = solvedSolutionFilesDirectory;
    }

    public boolean isSortSolverBenchmarks() {
        return sortSolverBenchmarks;
    }

    public void setSortSolverBenchmarks(boolean sortSolverBenchmarks) {
        this.sortSolverBenchmarks = sortSolverBenchmarks;
    }

    public Comparator<SolverBenchmark> getSolverBenchmarkComparator() {
        return solverBenchmarkComparator;
    }

    public void setSolverBenchmarkComparator(Comparator<SolverBenchmark> solverBenchmarkComparator) {
        this.solverBenchmarkComparator = solverBenchmarkComparator;
    }

    public SolverStatisticType getSolverStatisticType() {
        return solverStatisticType;
    }

    public void setSolverStatisticType(SolverStatisticType solverStatisticType) {
        this.solverStatisticType = solverStatisticType;
    }

    public File getSolverStatisticFilesDirectory() {
        return solverStatisticFilesDirectory;
    }

    public void setSolverStatisticFilesDirectory(File solverStatisticFilesDirectory) {
        this.solverStatisticFilesDirectory = solverStatisticFilesDirectory;
    }

    public LocalSearchSolverConfig getInheritedLocalSearchSolverConfig() {
        return inheritedLocalSearchSolverConfig;
    }

    public void setInheritedLocalSearchSolverConfig(LocalSearchSolverConfig inheritedLocalSearchSolverConfig) {
        this.inheritedLocalSearchSolverConfig = inheritedLocalSearchSolverConfig;
    }

    public List<File> getInheritedUnsolvedSolutionFileList() {
        return inheritedUnsolvedSolutionFileList;
    }

    public void setInheritedUnsolvedSolutionFileList(List<File> inheritedUnsolvedSolutionFileList) {
        this.inheritedUnsolvedSolutionFileList = inheritedUnsolvedSolutionFileList;
    }

    public List<SolverBenchmark> getSolverBenchmarkList() {
        return solverBenchmarkList;
    }

    public void setSolverBenchmarkList(List<SolverBenchmark> solverBenchmarkList) {
        this.solverBenchmarkList = solverBenchmarkList;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public void benchmarkingStarted() {
        Set<String> nameSet = new HashSet<String>(solverBenchmarkList.size());
        Set<SolverBenchmark> noNameBenchmarkSet = new LinkedHashSet<SolverBenchmark>(solverBenchmarkList.size());
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            if (solverBenchmark.getName() != null) {
                boolean unique = nameSet.add(solverBenchmark.getName());
                if (!unique) {
                    throw new IllegalStateException("The benchmark name (" + solverBenchmark.getName()
                            + ") is used in more than 1 benchmark.");
                }
            } else {
                noNameBenchmarkSet.add(solverBenchmark);
            }
            if (inheritedLocalSearchSolverConfig != null) {
                solverBenchmark.inheritLocalSearchSolverConfig(inheritedLocalSearchSolverConfig);
            }
            if (inheritedUnsolvedSolutionFileList != null) {
                solverBenchmark.inheritUnsolvedSolutionFileList(inheritedUnsolvedSolutionFileList);
            }
        }
        int generatedNameIndex = 0;
        for (SolverBenchmark solverBenchmark : noNameBenchmarkSet) {
            String generatedName = "Config_" + generatedNameIndex;
            while (nameSet.contains(generatedName)) {
                generatedNameIndex++;
                generatedName = "Config_" + generatedNameIndex;
            }
            solverBenchmark.setName(generatedName);
            generatedNameIndex++;
        }
    }

    public void benchmark(XStream xStream) { // TODO refactor out xstream
        benchmarkingStarted();
        if (solvedSolutionFilesDirectory != null) {
            solvedSolutionFilesDirectory.mkdirs();
        }
        Map<File, SolverStatistic> unsolvedSolutionFileToStatisticMap;
        if (solverStatisticType != SolverStatisticType.NONE) {
            unsolvedSolutionFileToStatisticMap = new HashMap<File, SolverStatistic>();
            if (solverStatisticFilesDirectory == null) {
                throw new IllegalArgumentException("With solverStatisticType (" + solverStatisticType
                        + ") the solverStatisticFilesDirectory must not be null.");
            }
            solverStatisticFilesDirectory.mkdirs();
        } else {
            unsolvedSolutionFileToStatisticMap = null;
        }
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            Solver solver = solverBenchmark.getLocalSearchSolverConfig().buildSolver();
            for (SolverBenchmarkResult result : solverBenchmark.getSolverBenchmarkResultList()) {
                File unsolvedSolutionFile = result.getUnsolvedSolutionFile();
                Solution unsolvedSolution = readUnsolvedSolution(xStream, unsolvedSolutionFile);
                solver.setStartingSolution(unsolvedSolution);
                if (solverStatisticType != SolverStatisticType.NONE) {
                    SolverStatistic statistic = unsolvedSolutionFileToStatisticMap.get(unsolvedSolutionFile);
                    if (statistic == null) {
                        statistic = solverStatisticType.create();
                        unsolvedSolutionFileToStatisticMap.put(unsolvedSolutionFile, statistic);
                    }
                    statistic.addListener(solver, solverBenchmark.getName());
                }
                solver.solve();
                result.setTimeMillesSpend(solver.getTimeMillisSpend());
                Solution solvedSolution = solver.getBestSolution();
                result.setScore(solvedSolution.getScore());
                if (solverStatisticType != SolverStatisticType.NONE) {
                    SolverStatistic statistic = unsolvedSolutionFileToStatisticMap.get(unsolvedSolutionFile);
                    statistic.removeListener(solver, solverBenchmark.getName());
                }
                writeSolvedSolution(xStream, solverBenchmark, result, solvedSolution);
            }
        }
        if (solverStatisticType != SolverStatisticType.NONE) {
            for (Map.Entry<File, SolverStatistic> entry : unsolvedSolutionFileToStatisticMap.entrySet()) {
                File unsolvedSolutionFile = entry.getKey();
                SolverStatistic statistic = entry.getValue();
                String baseName = FilenameUtils.getBaseName(unsolvedSolutionFile.getName());
                statistic.writeStatistic(solverStatisticFilesDirectory, baseName);
            }
        }
        benchmarkingEnded();
    }

    private Solution readUnsolvedSolution(XStream xStream, File unsolvedSolutionFile) {
        Solution unsolvedSolution;
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(unsolvedSolutionFile), "utf-8");
            unsolvedSolution = (Solution) xStream.fromXML(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem reading unsolvedSolutionFile: " + unsolvedSolutionFile, e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return unsolvedSolution;
    }

    private void writeSolvedSolution(XStream xStream, SolverBenchmark solverBenchmark, SolverBenchmarkResult result,
            Solution solvedSolution) {
        if (solvedSolutionFilesDirectory == null) {
            return;
        }
        File solvedSolutionFile = null;
        String baseName = FilenameUtils.getBaseName(result.getUnsolvedSolutionFile().getName());
        String solverBenchmarkName = solverBenchmark.getName().replaceAll(" ", "_").replaceAll("[^\\w\\d_\\-]", "");
        String scoreString = result.getScore().toString().replaceAll("[\\/ ]", "_");
        String timeString = TIME_FORMAT.format(result.getTimeMillesSpend()) + "ms";
        solvedSolutionFile = new File(solvedSolutionFilesDirectory, baseName + "_" + solverBenchmarkName
                + "_score" + scoreString + "_time" + timeString + ".xml");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(solvedSolutionFile), "utf-8");
            xStream.toXML(solvedSolution, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing solvedSolutionFile: " + solvedSolutionFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void benchmarkingEnded() {
        if (sortSolverBenchmarks) {
            if (solverBenchmarkComparator == null) {
                solverBenchmarkComparator = new MaxScoreSolverBenchmarkComparator();
            }
            Collections.sort(solverBenchmarkList, solverBenchmarkComparator);
        }
    }
    
    public static enum SolvedSolutionVerbosity {
        ALL
    }

    public static enum SolverStatisticType {
        NONE,
        BEST_SOLUTION_CHANGED;

        public SolverStatistic create() {
            switch (this) {
                case NONE:
                    return null;
                case BEST_SOLUTION_CHANGED:
                    return new BestScoreStatistic();
                default:
                    throw new IllegalStateException("The solverStatisticType (" + this + ") is not implemented");
            }
        }
    }

}
