package org.drools.solver.benchmark;

import java.io.File;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.solver.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("solverBenchmarkResult")
public class SolverBenchmarkResult {

    private File unsolvedSolutionFile = null;
    private Score score = null;
    private Long timeMillesSpend = null;
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

    public Long getTimeMillesSpend() {
        return timeMillesSpend;
    }

    public void setTimeMillesSpend(Long timeMillesSpend) {
        this.timeMillesSpend = timeMillesSpend;
    }

    public File getSolvedSolutionFile() {
        return solvedSolutionFile;
    }

    public void setSolvedSolutionFile(File solvedSolutionFile) {
        this.solvedSolutionFile = solvedSolutionFile;
    }
    
}
