package org.optaplanner.core.impl.phase.custom;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.phase.AbstractPhase;
import org.optaplanner.core.impl.phase.custom.scope.CustomPhaseScope;
import org.optaplanner.core.impl.phase.custom.scope.CustomStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.termination.Termination;

/**
 * Default implementation of {@link CustomPhase}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class DefaultCustomPhase<Solution_> extends AbstractPhase<Solution_> implements CustomPhase<Solution_> {

    protected final List<CustomPhaseCommand<Solution_>> customPhaseCommandList;

    private final String phaseTypeString;

    protected DefaultCustomPhase(Builder<Solution_> builder) {
        super(builder);
        this.customPhaseCommandList = builder.customPhaseCommandList;
        this.phaseTypeString = builder.phaseTypeString;
    }

    @Override
    public String getPhaseTypeString() {
        return phaseTypeString;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solve(SolverScope<Solution_> solverScope) {
        CustomPhaseScope<Solution_> phaseScope = new CustomPhaseScope<>(solverScope);
        phaseStarted(phaseScope);

        CustomStepScope<Solution_> stepScope = new CustomStepScope<>(phaseScope);
        for (CustomPhaseCommand<Solution_> customPhaseCommand : customPhaseCommandList) {
            solverScope.checkYielding();
            if (phaseTermination.isPhaseTerminated(phaseScope)) {
                break;
            }
            stepStarted(stepScope);
            doStep(stepScope, customPhaseCommand);
            stepEnded(stepScope);
            phaseScope.setLastCompletedStepScope(stepScope);
            stepScope = new CustomStepScope<>(phaseScope);
        }
        phaseEnded(phaseScope);
    }

    public void phaseStarted(CustomPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
    }

    public void stepStarted(CustomStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
    }

    private void doStep(CustomStepScope<Solution_> stepScope, CustomPhaseCommand<Solution_> customPhaseCommand) {
        InnerScoreDirector<Solution_, ?> scoreDirector = stepScope.getScoreDirector();
        customPhaseCommand.changeWorkingSolution(scoreDirector);
        calculateWorkingStepScore(stepScope, customPhaseCommand);
        solver.getBestSolutionRecaller().processWorkingSolutionDuringStep(stepScope);
    }

    public void stepEnded(CustomStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        boolean bestScoreImproved = stepScope.getBestScoreImproved();
        if (!bestScoreImproved) {
            solver.getBestSolutionRecaller().updateBestSolutionAndFire(stepScope.getPhaseScope().getSolverScope());
        }
        CustomPhaseScope<Solution_> phaseScope = stepScope.getPhaseScope();
        if (logger.isDebugEnabled()) {
            logger.debug("{}    {} step ({}), time spent ({}), score ({}), {} best score ({}).",
                    logIndentation,
                    phaseTypeString,
                    stepScope.getStepIndex(),
                    phaseScope.calculateSolverTimeMillisSpentUpToNow(),
                    stepScope.getScore(),
                    bestScoreImproved ? "new" : "   ",
                    phaseScope.getBestScore());
        }
    }

    public void phaseEnded(CustomPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        phaseScope.endingNow();
        logger.info("{}{} phase ({}) ended: time spent ({}), best score ({}),"
                + " score calculation speed ({}/sec), step total ({}).",
                logIndentation,
                phaseTypeString,
                phaseIndex,
                phaseScope.calculateSolverTimeMillisSpentUpToNow(),
                phaseScope.getBestScore(),
                phaseScope.getPhaseScoreCalculationSpeed(),
                phaseScope.getNextStepIndex());
    }

    public static class Builder<Solution_> extends AbstractPhase.Builder<Solution_> {

        private final List<CustomPhaseCommand<Solution_>> customPhaseCommandList;

        private final String phaseTypeString;

        public Builder(int phaseIndex, String logIndentation, Termination<Solution_> phaseTermination,
                List<CustomPhaseCommand<Solution_>> customPhaseCommandList, String phaseTypeString) {
            super(phaseIndex, logIndentation, phaseTermination);
            this.customPhaseCommandList = List.copyOf(customPhaseCommandList);
            this.phaseTypeString = phaseTypeString;
        }

        public Builder(int phaseIndex, String logIndentation, Termination<Solution_> phaseTermination,
                List<CustomPhaseCommand<Solution_>> customPhaseCommandList) {
            super(phaseIndex, logIndentation, phaseTermination);
            this.customPhaseCommandList = List.copyOf(customPhaseCommandList);
            this.phaseTypeString = "Custom";
        }

        @Override
        public DefaultCustomPhase<Solution_> build() {
            return new DefaultCustomPhase<>(this);
        }
    }
}
