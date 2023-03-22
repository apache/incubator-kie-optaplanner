package org.optaplanner.core.impl.ruin;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.phase.AbstractPhase;
import org.optaplanner.core.impl.phase.custom.CustomPhase;
import org.optaplanner.core.impl.phase.custom.scope.CustomPhaseScope;
import org.optaplanner.core.impl.phase.custom.scope.CustomStepScope;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.termination.Termination;

/**
 * Default implementation of {@link RuinPhase}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class RuinPhase<Solution_> extends AbstractPhase<Solution_> implements CustomPhase<Solution_> {

    protected final RuinPhaseCommand<Solution_> ruinPhaseCommand;

    private RuinPhase(Builder<Solution_> builder) {
        super(builder);
        ruinPhaseCommand = builder.ruinPhaseCommand;
    }

    @Override
    public String getPhaseTypeString() {
        return "Ruin";
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solve(SolverScope<Solution_> solverScope) {
        CustomPhaseScope<Solution_> phaseScope = new CustomPhaseScope<>(solverScope);
        phaseStarted(phaseScope);

        CustomStepScope<Solution_> stepScope = new CustomStepScope<>(phaseScope);

        solverScope.checkYielding();
        stepStarted(stepScope);
        doStep(stepScope, ruinPhaseCommand);
        stepEnded(stepScope);
        phaseScope.setLastCompletedStepScope(stepScope);

        phaseEnded(phaseScope);
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        ruinPhaseCommand.solvingStarted(solverScope);
    }

    @Override
    public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        ruinPhaseCommand.phaseStarted(phaseScope);
    }

    @Override
    public void stepStarted(AbstractStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        ruinPhaseCommand.stepStarted(stepScope);
    }

    private void doStep(CustomStepScope<Solution_> stepScope, RuinPhaseCommand<Solution_> ruinPhaseCommand) {
        InnerScoreDirector<Solution_, ?> scoreDirector = stepScope.getScoreDirector();
        ruinPhaseCommand.changeWorkingSolution(scoreDirector);
        calculateWorkingStepScore(stepScope, ruinPhaseCommand);
        solver.getBestSolutionRecaller().processWorkingSolutionDuringStep(stepScope);
    }

    public void stepEnded(CustomStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        ruinPhaseCommand.stepEnded(stepScope);
        solver.getBestSolutionRecaller().updateBestSolutionAndFire(stepScope.getPhaseScope().getSolverScope());
        if (logger.isDebugEnabled()) {
            CustomPhaseScope<Solution_> phaseScope = stepScope.getPhaseScope();
            logger.debug("{}    Ruin step ({}), time spent ({}), score ({}), {} best score ({}).",
                    logIndentation,
                    stepScope.getStepIndex(),
                    phaseScope.calculateSolverTimeMillisSpentUpToNow(),
                    stepScope.getScore(),
                    "new",
                    phaseScope.getBestScore());
        }
    }

    public void phaseEnded(CustomPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        ruinPhaseCommand.phaseEnded(phaseScope);
        phaseScope.endingNow();
        logger.info("{}Ruin phase ({}) ended: time spent ({}), best score ({}),"
                + " score calculation speed ({}/sec), step total ({}).",
                logIndentation,
                phaseIndex,
                phaseScope.calculateSolverTimeMillisSpentUpToNow(),
                phaseScope.getBestScore(),
                phaseScope.getPhaseScoreCalculationSpeed(),
                phaseScope.getNextStepIndex());
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        ruinPhaseCommand.solvingEnded(solverScope);
    }

    public static class Builder<Solution_> extends AbstractPhase.Builder<Solution_> {

        private final RuinPhaseCommand<Solution_> ruinPhaseCommand;

        public Builder(int phaseIndex, String logIndentation, Termination<Solution_> phaseTermination,
                RuinPhaseCommand<Solution_> ruinPhaseCommand) {
            super(phaseIndex, logIndentation, phaseTermination);
            this.ruinPhaseCommand = ruinPhaseCommand;
        }

        @Override
        public RuinPhase<Solution_> build() {
            return new RuinPhase<>(this);
        }
    }
}
