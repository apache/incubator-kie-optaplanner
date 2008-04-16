package org.drools.solver.core.localsearch.decider.forager;

import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.move.DummyMove;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class MaxScoreOfAllForagerTest extends TestCase {

    public void testPicking() {
        Forager forager = new MaxScoreOfAllForager();
        LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope();
        localSearchSolverScope.setWorkingRandom(new Random());
        forager.solvingStarted(localSearchSolverScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        forager.beforeDeciding(stepScope);
        Move a = new DummyMove();
        Move b = new DummyMove();
        Move c = new DummyMove();
        Move d = new DummyMove();
        Move e = new DummyMove();
        forager.addMove(createMoveScope(stepScope, a, -100.0, 10000.0));
        forager.addMove(createMoveScope(stepScope, b, -90.0, 2000.0));
        forager.addMove(createMoveScope(stepScope, c, -100.0, 300.0));
        forager.addMove(createMoveScope(stepScope, d, -190.0, 40.0));
        forager.addMove(createMoveScope(stepScope, e, -90.0, 5.0));
        MoveScope pickedScope = forager.pickMove(stepScope);
        Move picked = pickedScope.getMove();
        assertTrue(picked == b || picked == e);
        List<Move> topList = forager.getTopList(3);
        assertTrue(topList.contains(a));
        assertTrue(topList.contains(b));
        assertFalse(topList.contains(c));
        assertFalse(topList.contains(d));
        assertTrue(topList.contains(e));
        forager.solvingEnded(localSearchSolverScope);
    }

    public MoveScope createMoveScope(StepScope stepScope, Move move, double score, double acceptChance) {
        MoveScope moveScope = new MoveScope(stepScope);
        moveScope.setMove(move);
        moveScope.setScore(score);
        moveScope.setAcceptChance(acceptChance);
        return moveScope;
    }

}
