package org.drools.planner.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.drools.planner.config.localsearch.LocalSearchSolverConfig;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("solverBenchmark")
public class SolverBenchmark {

    private String name = null;

    @XStreamAlias("localSearchSolver")
    private LocalSearchSolverConfig localSearchSolverConfig = null;
    @XStreamImplicit(itemFieldName = "unsolvedSolutionFile")
    private List<File> unsolvedSolutionFileList = null;

    @XStreamImplicit(itemFieldName = "solverBenchmarkResult")
    private List<SolverBenchmarkResult> solverBenchmarkResultList = null;

    // Ranking starts from 0
    private Integer ranking = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalSearchSolverConfig getLocalSearchSolverConfig() {
        return localSearchSolverConfig;
    }

    public void setLocalSearchSolverConfig(LocalSearchSolverConfig localSearchSolverConfig) {
        this.localSearchSolverConfig = localSearchSolverConfig;
    }

    public List<File> getUnsolvedSolutionFileList() {
        return unsolvedSolutionFileList;
    }

    public void setUnsolvedSolutionFileList(List<File> unsolvedSolutionFileList) {
        this.unsolvedSolutionFileList = unsolvedSolutionFileList;
    }

    public List<SolverBenchmarkResult> getSolverBenchmarkResultList() {
        return solverBenchmarkResultList;
    }

    public void setSolverBenchmarkResultList(List<SolverBenchmarkResult> solverBenchmarkResultList) {
        this.solverBenchmarkResultList = solverBenchmarkResultList;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public void inheritLocalSearchSolverConfig(LocalSearchSolverConfig inheritedLocalSearchSolverConfig) {
        if (localSearchSolverConfig == null) {
            localSearchSolverConfig = inheritedLocalSearchSolverConfig;
        } else {
            localSearchSolverConfig.inherit(inheritedLocalSearchSolverConfig);
        }
    }

    public void inheritUnsolvedSolutionFileList(List<File> inheritedUnsolvedSolutionFileList) {
        List<File> filesWithResult = new ArrayList<File>();
        if (solverBenchmarkResultList != null) {
            for (SolverBenchmarkResult result : solverBenchmarkResultList) {
                filesWithResult.add(result.getUnsolvedSolutionFile());
            }
        } else {
            solverBenchmarkResultList = new ArrayList<SolverBenchmarkResult>();
        }
        if (unsolvedSolutionFileList != null) {
            for (File unsolvedSolutionFile : unsolvedSolutionFileList) {
                if (!filesWithResult.contains(unsolvedSolutionFile)) {
                    SolverBenchmarkResult result = new SolverBenchmarkResult();
                    result.setUnsolvedSolutionFile(unsolvedSolutionFile);
                    solverBenchmarkResultList.add(result);
                    filesWithResult.add(unsolvedSolutionFile);
                }
            }
        }
        if (inheritedUnsolvedSolutionFileList != null) {
            for (File inheritedUnsolvedSolutionFile : inheritedUnsolvedSolutionFileList) {
                if (!filesWithResult.contains(inheritedUnsolvedSolutionFile)) {
                    SolverBenchmarkResult result = new SolverBenchmarkResult();
                    result.setUnsolvedSolutionFile(inheritedUnsolvedSolutionFile);
                    solverBenchmarkResultList.add(result);
                    filesWithResult.add(inheritedUnsolvedSolutionFile);
                }
            }
        }
    }

    public List<Score> getScoreList() {
        List<Score> scoreList = new ArrayList<Score>(solverBenchmarkResultList.size());
        for (SolverBenchmarkResult solverBenchmarkResult : solverBenchmarkResultList) {
            scoreList.add(solverBenchmarkResult.getScore());
        }
        return scoreList;
    }

    /**
     * @return the total score
     */
    public Score getTotalScore() {
        Score totalScore = null;
        for (SolverBenchmarkResult solverBenchmarkResult : solverBenchmarkResultList) {
            if (totalScore == null) {
                totalScore = solverBenchmarkResult.getScore();
            } else {
                totalScore = totalScore.add(solverBenchmarkResult.getScore());
            }
        }
        return totalScore;
    }

    /**
     * @return the average score
     */
    public Score getAverageScore() {
        return getTotalScore().divide(solverBenchmarkResultList.size());
    }

}
