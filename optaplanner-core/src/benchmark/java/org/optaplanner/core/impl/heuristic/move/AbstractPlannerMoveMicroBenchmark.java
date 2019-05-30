package org.optaplanner.core.impl.heuristic.move;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.optaplanner.core.impl.score.director.ScoreDirector;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 5, time = 5)
@Fork(value = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public abstract class AbstractPlannerMoveMicroBenchmark<Solution> {

    private Move<Solution> move;
    private ScoreDirector<Solution> scoreDirector;

    public ScoreDirector<Solution> getScoreDirector() {
        return scoreDirector;
    }

    public void setScoreDirector(ScoreDirector<Solution> scoreDirector) {
        this.scoreDirector = scoreDirector;
    }

    public Move<Solution> getMove() {
        return move;
    }

    public void setMove(Move<Solution> move) {
        this.move = move;
    }

    protected abstract void initScoreDirector();

    protected abstract void initMove();

    protected abstract void initEntities();

    @Setup(Level.Invocation)
    public void initBenchmark() {
        initEntities();
        initMove();
        initScoreDirector();
    }

    public Move<Solution> benchmarkDoMove() {
        return getMove().doMove(getScoreDirector());
    }

    public Move<Solution> benchmarkRebase() {
        return getMove().rebase(getScoreDirector());
    }
}
