package org.drools.planner.benchmark;

import java.io.File;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("solverBenchmarkResult")
public class SolverBenchmarkResult {

    private File unsolvedSolutionFile = null;
    private Score score = null;
    private Long timeMillisSpend = null;
    private File solvedSolutionFile = null;

    public File getUnsolvedSolutionFile() {
        return unsolvedSolutionFile;
    }

    public void setUnsolvedSolutionFile(File unsolvedSolutionFile) {
        this.unsolvedSolutionFile = unsolvedSolutionFile;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Long getTimeMillisSpend() {
        return timeMillisSpend;
    }

    public void setTimeMillisSpend(Long timeMillisSpend) {
        this.timeMillisSpend = timeMillisSpend;
    }

    public File getSolvedSolutionFile() {
        return solvedSolutionFile;
    }

    public void setSolvedSolutionFile(File solvedSolutionFile) {
        this.solvedSolutionFile = solvedSolutionFile;
    }
    
}
