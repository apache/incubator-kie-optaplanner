package org.optaplanner.core.impl.ruin;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.move.NoChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;

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
            // no more entities to be ruined found - the solution is already fully ruined
            NoChangeMove<Solution_> noChangeMove = new NoChangeMove<>();
            noChangeMove.doMoveOnly(scoreDirector);
        }
    }
}
