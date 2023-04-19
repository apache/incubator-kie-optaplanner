package org.optaplanner.core.impl.ruin;

import java.util.Collections;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.phase.custom.CustomPhase;
import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;
import org.optaplanner.core.impl.phase.custom.DefaultCustomPhase;
import org.optaplanner.core.impl.phase.custom.scope.CustomPhaseScope;
import org.optaplanner.core.impl.phase.custom.scope.CustomStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.termination.Termination;

/**
 * Ruin implementation of {@link CustomPhase}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class RuinCustomPhase<Solution_, Score_ extends Score<Score_>> extends DefaultCustomPhase<Solution_> {

    private Solution_ bestSolutionBeforeRuin;
    private Score_ bestScoreBeforeRuin;
    private final MoveSelector<Solution_> moveSelector;

    protected RuinCustomPhase(Builder<Solution_> builder) {
        super(builder);
        this.moveSelector = builder.moveSelector;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        moveSelector.solvingStarted(solverScope);
    }

    public void phaseStarted(CustomPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        moveSelector.phaseStarted(phaseScope);
        bestSolutionBeforeRuin = phaseScope.getScoreDirector().cloneWorkingSolution();
        bestScoreBeforeRuin = (Score_) phaseScope.getScoreDirector().calculateScore();
    }

    public void stepStarted(CustomStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        moveSelector.stepStarted(stepScope);
    }

    public void stepEnded(CustomStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        moveSelector.stepEnded(stepScope);
    }

    public void phaseEnded(CustomPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        moveSelector.phaseStarted(phaseScope);
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        if (bestScoreBeforeRuin.compareTo((Score_) solverScope.getBestScore()) > 0) {
            solverScope.setBestSolution(bestSolutionBeforeRuin);
            solverScope.setBestScore(bestScoreBeforeRuin);
            solverScope.setBestSolutionTimeMillis(System.currentTimeMillis());
            solverScope.getScoreDirector().setWorkingSolution(bestSolutionBeforeRuin);
            solver.getBestSolutionRecaller().updateBestSolutionAndFire(solverScope);
        }
        super.solvingEnded(solverScope);
        moveSelector.solvingEnded(solverScope);
    }

    public static class Builder<Solution_> extends DefaultCustomPhase.Builder<Solution_> {

        private final MoveSelector<Solution_> moveSelector;

        public Builder(int phaseIndex, String logIndentation, Termination<Solution_> phaseTermination,
                CustomPhaseCommand<Solution_> ruinPhaseCommand, MoveSelector<Solution_> moveSelector) {
            super(phaseIndex, logIndentation, phaseTermination, Collections.singletonList(ruinPhaseCommand), "Ruin");
            this.moveSelector = moveSelector;
        }

        @Override
        public RuinCustomPhase<Solution_, ?> build() {
            return new RuinCustomPhase<>(this);
        }
    }
}
