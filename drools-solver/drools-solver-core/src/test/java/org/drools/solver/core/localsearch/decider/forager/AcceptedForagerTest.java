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
public class AcceptedForagerTest extends TestCase {

    public void testPickMoveMaxScoreOfAll() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyByScore.NONE, false);
        LocalSearchSolverScope localSearchSolverScope = createLocalSearchSolverScope();
        forager.solvingStarted(localSearchSolverScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        forager.beforeDeciding(stepScope);
        // Pre conditions
        MoveScope a = createMoveScope(stepScope, -20.0, 30.0);
        MoveScope b = createMoveScope(stepScope, -1.0, 9.0);
        MoveScope c = createMoveScope(stepScope, -20.0, 20.0);
        MoveScope d = createMoveScope(stepScope, -300.0, 50000.0);
        MoveScope e = createMoveScope(stepScope, -1.0, 1.0);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertFalse(forager.isQuitEarly());
        forager.addMove(e);
        assertFalse(forager.isQuitEarly());
        MoveScope pickedScope = forager.pickMove(stepScope);
        // Post conditions
        assertSame(b, pickedScope);
        List<Move> topList = forager.getTopList(3);
        assertTrue(topList.contains(a.getMove())); // Because a's acceptChance is higher than c's
        assertTrue(topList.contains(b.getMove()));
        assertFalse(topList.contains(c.getMove()));
        assertFalse(topList.contains(d.getMove()));
        assertTrue(topList.contains(e.getMove()));
        forager.solvingEnded(localSearchSolverScope);
    }

    public void testPickMoveFirstBestScoreImproving() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyByScore.FIRST_BEST_SCORE_IMPROVING, false);
        LocalSearchSolverScope localSearchSolverScope = createLocalSearchSolverScope();
        forager.solvingStarted(localSearchSolverScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        forager.beforeDeciding(stepScope);
        // Pre conditions
        MoveScope a = createMoveScope(stepScope, -1.0, 0.0);
        MoveScope b = createMoveScope(stepScope, -20.0, 1.0);
        MoveScope c = createMoveScope(stepScope, -300.0, 1.0);
        MoveScope d = createMoveScope(stepScope, -1.0, 0.3);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertTrue(forager.isQuitEarly());
        // Post conditions
        MoveScope pickedScope = forager.pickMove(stepScope);
        assertSame(d, pickedScope);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b.getMove()));
        assertTrue(topList.contains(d.getMove()));
        forager.solvingEnded(localSearchSolverScope);
    }

    public void testPickMoveFirstLastStepScoreImproving() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyByScore.FIRST_LAST_STEP_SCORE_IMPROVING, false);
        LocalSearchSolverScope localSearchSolverScope = createLocalSearchSolverScope();
        forager.solvingStarted(localSearchSolverScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        forager.beforeDeciding(stepScope);
        // Pre conditions
        MoveScope a = createMoveScope(stepScope, -1.0, 0.0);
        MoveScope b = createMoveScope(stepScope, -300.0, 1.0);
        MoveScope c = createMoveScope(stepScope, -4000.0, 1.0);
        MoveScope d = createMoveScope(stepScope, -20.0, 0.3);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertTrue(forager.isQuitEarly());
        // Post conditions
        MoveScope pickedScope = forager.pickMove(stepScope);
        assertSame(d, pickedScope);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b.getMove()));
        assertTrue(topList.contains(d.getMove()));
        forager.solvingEnded(localSearchSolverScope);
    }

    public void testPickMoveRandomly() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyByScore.NONE, true);
        LocalSearchSolverScope localSearchSolverScope = createLocalSearchSolverScope();
        forager.solvingStarted(localSearchSolverScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        forager.beforeDeciding(stepScope);
        // Pre conditions
        MoveScope a = createMoveScope(stepScope, -20.0, 0.0);
        MoveScope b = createMoveScope(stepScope, -1.0, 0.1);
        MoveScope c = createMoveScope(stepScope, -1.0, 0.0);
        MoveScope d = createMoveScope(stepScope, -20.0, 0.3);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertTrue(forager.isQuitEarly());
        // Post conditions
        MoveScope pickedScope = forager.pickMove(stepScope);
        assertSame(d, pickedScope);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b.getMove()));
        assertTrue(topList.contains(d.getMove()));
        forager.solvingEnded(localSearchSolverScope);
    }

    private LocalSearchSolverScope createLocalSearchSolverScope() {
        LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope();
        localSearchSolverScope.setWorkingRandom(new Random() {
            public double nextDouble() {
                return 0.2;
            }
        });
        localSearchSolverScope.setBestScore(-10.0);
        StepScope lastStepScope = new StepScope(localSearchSolverScope);
        lastStepScope.setScore(-100.0);
        localSearchSolverScope.setLastCompletedStepScope(lastStepScope);
        return localSearchSolverScope;
    }

    public MoveScope createMoveScope(StepScope stepScope, double score, double acceptChance) {
        MoveScope moveScope = new MoveScope(stepScope);
        moveScope.setMove(new DummyMove());
        moveScope.setScore(score);
        moveScope.setAcceptChance(acceptChance);
        return moveScope;
    }

}