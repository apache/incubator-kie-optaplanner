package org.optaplanner.core.impl.ruin;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.move.NoChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListener;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;

/**
 * Ruins solution.
 */
public class RuinPhaseCommand<Solution_> implements CustomPhaseCommand<Solution_>, PhaseLifecycleListener<Solution_> {

    private final MoveSelector<Solution_> moveSelector;

    public RuinPhaseCommand(MoveSelector<Solution_> moveSelector) {
        this.moveSelector = moveSelector;
    }

    @Override
    public void changeWorkingSolution(ScoreDirector<Solution_> scoreDirector) {
        Move<Solution_> nextRuinMove = moveSelector.iterator().next();
        if (nextRuinMove != null) {
            nextRuinMove.doMoveOnly(scoreDirector);
            scoreDirector.triggerVariableListeners();
        } else {
            // no more entities to be ruined found - the solution is already fully ruined
            NoChangeMove<Solution_> noChangeMove = new NoChangeMove<>();
            noChangeMove.doMoveOnly(scoreDirector);
        }
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        moveSelector.solvingStarted(solverScope);
    }

    @Override
    public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
        moveSelector.phaseStarted(phaseScope);
    }

    @Override
    public void stepStarted(AbstractStepScope<Solution_> stepScope) {
        moveSelector.stepStarted(stepScope);
    }

    @Override
    public void stepEnded(AbstractStepScope<Solution_> stepScope) {
        moveSelector.stepEnded(stepScope);
    }

    @Override
    public void phaseEnded(AbstractPhaseScope<Solution_> phaseScope) {
        moveSelector.phaseEnded(phaseScope);
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        moveSelector.solvingEnded(solverScope);
    }
}
