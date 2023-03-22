package org.optaplanner.core.impl.ruin;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.move.NoChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;
import org.optaplanner.core.impl.phase.custom.scope.CustomPhaseScope;
import org.optaplanner.core.impl.phase.custom.scope.CustomStepScope;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;

/**
 * Ruins solution.
 */
public class RuinPhaseCommand<Solution_> implements CustomPhaseCommand<Solution_> {

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
            NoChangeMove<Solution_> noChangeMove = new NoChangeMove<>();
            noChangeMove.doMoveOnly(scoreDirector);
        }
    }

    public void solvingStarted(SolverScope<Solution_> solverScope) {
        moveSelector.solvingStarted(solverScope);
    }

    public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
        moveSelector.phaseStarted(phaseScope);
    }

    public void stepStarted(AbstractStepScope<Solution_> stepScope) {
        moveSelector.stepStarted(stepScope);
    }

    public void stepEnded(CustomStepScope<Solution_> stepScope) {
        moveSelector.stepEnded(stepScope);
    }

    public void phaseEnded(CustomPhaseScope<Solution_> phaseScope) {
        moveSelector.phaseEnded(phaseScope);
    }

    public void solvingEnded(SolverScope<Solution_> solverScope) {
        moveSelector.solvingEnded(solverScope);
    }
}
