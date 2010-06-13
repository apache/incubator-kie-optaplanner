package org.drools.planner.benchmark;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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

import javax.imageio.ImageIO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.drools.planner.config.localsearch.LocalSearchSolverConfig;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.definition.ScoreDefinition;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.benchmark.statistic.BestScoreStatistic;
import org.drools.planner.benchmark.statistic.SolverStatistic;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("solverBenchmarkSuite")
public class SolverBenchmarkSuite {

    public static final NumberFormat TIME_FORMAT = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private File benchmarkDirectory = null;
    private File solvedSolutionFilesDirectory = null;
    private File solverStatisticFilesDirectory = null;
    private SolverStatisticType solverStatisticType = SolverStatisticType.NONE;
    private boolean sortSolverBenchmarks = true;
    private Comparator<SolverBenchmark> solverBenchmarkComparator = null;

    @XStreamAlias("inheritedLocalSearchSolver")
    private LocalSearchSolverConfig inheritedLocalSearchSolverConfig = null;
    @XStreamImplicit(itemFieldName = "inheritedUnsolvedSolutionFile")
    private List<File> inheritedUnsolvedSolutionFileList = null;

    @XStreamImplicit(itemFieldName = "solverBenchmark")
    private List<SolverBenchmark> solverBenchmarkList = null;


    public File getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public void setBenchmarkDirectory(File benchmarkDirectory) {
        this.benchmarkDirectory = benchmarkDirectory;
    }

    public File getSolvedSolutionFilesDirectory() {
        return solvedSolutionFilesDirectory;
    }

    public void setSolvedSolutionFilesDirectory(File solvedSolutionFilesDirectory) {
        this.solvedSolutionFilesDirectory = solvedSolutionFilesDirectory;
    }

    public File getSolverStatisticFilesDirectory() {
        return solverStatisticFilesDirectory;
    }

    public void setSolverStatisticFilesDirectory(File solverStatisticFilesDirectory) {
        this.solverStatisticFilesDirectory = solverStatisticFilesDirectory;
    }

    public SolverStatisticType getSolverStatisticType() {
        return solverStatisticType;
    }

    public void setSolverStatisticType(SolverStatisticType solverStatisticType) {
        this.solverStatisticType = solverStatisticType;
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
        if (benchmarkDirectory == null) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must not be null.");
        }
        benchmarkDirectory.mkdirs();
        if (solvedSolutionFilesDirectory == null) {
            solvedSolutionFilesDirectory = new File(benchmarkDirectory, "solved");
        }
        solvedSolutionFilesDirectory.mkdirs();
        if (solverStatisticFilesDirectory == null) {
            solverStatisticFilesDirectory = new File(benchmarkDirectory, "statistic");
        }
        solverStatisticFilesDirectory.mkdirs();
    }

    public void benchmark(XStream xStream) { // TODO refactor out xstream
        benchmarkingStarted();
        Map<File, SolverStatistic> unsolvedSolutionFileToStatisticMap = new HashMap<File, SolverStatistic>();
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
                result.setTimeMillisSpend(solver.getTimeMillisSpend());
                Solution solvedSolution = solver.getBestSolution();
                result.setScore(solvedSolution.getScore());
                if (solverStatisticType != SolverStatisticType.NONE) {
                    SolverStatistic statistic = unsolvedSolutionFileToStatisticMap.get(unsolvedSolutionFile);
                    statistic.removeListener(solver, solverBenchmark.getName());
                }
                writeSolvedSolution(xStream, solverBenchmark, result, solvedSolution);
            }
        }
        benchmarkingEnded(xStream, unsolvedSolutionFileToStatisticMap);
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
        String timeString = TIME_FORMAT.format(result.getTimeMillisSpend()) + "ms";
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

    public void benchmarkingEnded(XStream xStream, Map<File, SolverStatistic> unsolvedSolutionFileToStatisticMap) {
        if (sortSolverBenchmarks) {
            if (solverBenchmarkComparator == null) {
                solverBenchmarkComparator = new AverageScoreSolverBenchmarkComparator();
            }
            Collections.sort(solverBenchmarkList, solverBenchmarkComparator);
            Collections.reverse(solverBenchmarkList); // Best results first, worst results last
        }
        writeBestScoreSummary();
        // 2 lines at 80 chars per line give a max of 160 per entry
        StringBuilder htmlFragment = new StringBuilder(unsolvedSolutionFileToStatisticMap.size() * 160);
        htmlFragment.append("  <h1>Summary</h1>\n");
        htmlFragment.append(writeBestScoreSummary());
        htmlFragment.append("  <h1>Statistic ").append(solverStatisticType.toString()).append("</h1>\n");
        for (Map.Entry<File, SolverStatistic> entry : unsolvedSolutionFileToStatisticMap.entrySet()) {
            File unsolvedSolutionFile = entry.getKey();
            SolverStatistic statistic = entry.getValue();
            String baseName = FilenameUtils.getBaseName(unsolvedSolutionFile.getName());
            htmlFragment.append("  <h2>").append(baseName).append("</h2>\n");
            htmlFragment.append(statistic.writeStatistic(solverStatisticFilesDirectory, baseName));
        }
        writeHtmlOverview(htmlFragment);
        writeBenchmarkResult(xStream);
    }

    private CharSequence writeBestScoreSummary() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            ScoreDefinition scoreDefinition = solverBenchmark.getLocalSearchSolverConfig().getScoreDefinitionConfig()
                    .buildScoreDefinition();
            for (SolverBenchmarkResult result : solverBenchmark.getSolverBenchmarkResultList()) {
                Double scoreGraphValue = scoreDefinition.translateScoreToGraphValue(result.getScore());
                dataset.addValue(scoreGraphValue, solverBenchmark.getName(), result.getUnsolvedSolutionFile().getName());
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(
            "Best score summary (higher score is better)", "Data", "Score",
            dataset, PlotOrientation.VERTICAL, true, true, false
        );
        BufferedImage chartImage = chart.createBufferedImage(1024, 768);
        File chartSummaryFile = new File(solverStatisticFilesDirectory, "summary.png");
        OutputStream out = null;
        try {
            out = new FileOutputStream(chartSummaryFile);
            ImageIO.write(chartImage, "png", out);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing graphStatisticFile: " + chartSummaryFile, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        return "  <img src=\"" + chartSummaryFile.getName() + "\"/>\n";
    }

    private void writeHtmlOverview(CharSequence htmlFragment) {
        File htmlOverviewFile = new File(solverStatisticFilesDirectory, "index.html");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(htmlOverviewFile), "utf-8");
            writer.append("<html>\n");
            writer.append("<head>\n");
            writer.append("  <title>Statistic</title>\n");
            writer.append("</head>\n");
            writer.append("<body>\n");
            writer.append(htmlFragment);
            writer.append("</body>\n");
            writer.append("</html>\n");
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing htmlOverviewFile: " + htmlOverviewFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void writeBenchmarkResult(XStream xStream) {
        File benchmarkResultFile = new File(benchmarkDirectory, "benchmarkResult.xml");
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(benchmarkResultFile), "utf-8");
            xStream.toXML(this, writer);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("This JVM does not support utf-8 encoding.", e);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                    "Could not create benchmarkResultFile (" + benchmarkResultFile + ").", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
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
