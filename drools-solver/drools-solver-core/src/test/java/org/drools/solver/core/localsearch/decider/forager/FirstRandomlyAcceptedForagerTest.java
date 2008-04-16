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
public class FirstRandomlyAcceptedForagerTest extends TestCase {

    public void testPicking() {
        Forager forager = new FirstRandomlyAcceptedForager();
        LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope();
        localSearchSolverScope.setWorkingRandom(new Random() {
                    public double nextDouble() {
                        return 0.99;
                    }
                });
        forager.solvingStarted(localSearchSolverScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        forager.beforeDeciding(stepScope);
        Move a = new DummyMove();
        Move b = new DummyMove();
        Move c = new DummyMove();
        Move d = new DummyMove();
        forager.addMove(createMoveScope(stepScope, a, -100.0, 0.0));
        assertFalse(forager.isQuitEarly());
        forager.addMove(createMoveScope(stepScope, b, -10.0, 0.8));
        assertFalse(forager.isQuitEarly());
        forager.addMove(createMoveScope(stepScope, c, -10.0, 0.0));
        assertFalse(forager.isQuitEarly());
        forager.addMove(createMoveScope(stepScope, d, -100.0, 1.0));
        assertTrue(forager.isQuitEarly());
        MoveScope pickedScope = forager.pickMove(stepScope);
        Move picked = pickedScope.getMove();
        assertTrue(picked == d);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b));
        assertTrue(topList.contains(d));
        forager.solvingEnded(localSearchSolverScope);
    }

    public MoveScope createMoveScope(StepScope stepScope, Move move, double score, double acceptChance) {
        // TODO duplicate code from MaxScoreOfAllForagerTest
        MoveScope moveScope = new MoveScope(stepScope);
        moveScope.setMove(move);
        moveScope.setScore(score);
        moveScope.setAcceptChance(acceptChance);
        return moveScope;
    }

}