package org.drools.solver.core.localsearch.decider.accepter.greatdeluge;

import java.util.Random;

import junit.framework.TestCase;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.localsearch.decider.accepter.Accepter;
import org.drools.solver.core.move.DummyMove;

/**
 * @author Geoffrey De Smet
 */
public class GreatDelugeAccepterTest extends TestCase {

    public void testCalculateAcceptChance() {
        // Setup
        Accepter accepter = new GreatDelugeAccepter(1.20, 0.01);
        LocalSearchSolverScope localSearchSolverScope = createLocalSearchSolverScope();
        accepter.solvingStarted(localSearchSolverScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        stepScope.setStepIndex(0);
        accepter.beforeDeciding(stepScope);
        // Pre conditions
        MoveScope a1 = createMoveScope(stepScope, -2000.0);
        MoveScope a2 = createMoveScope(stepScope, -1300.0);
        MoveScope a3 = createMoveScope(stepScope, -1200.0);
        MoveScope b1 = createMoveScope(stepScope, -1200.0);
        MoveScope b2 = createMoveScope(stepScope, -100.0);
        MoveScope c1 = createMoveScope(stepScope, -1100.0);
        MoveScope c2 = createMoveScope(stepScope, -120.0);
        // Do stuff
        assertEquals(0.0, accepter.calculateAcceptChance(a1));
        assertEquals(0.0, accepter.calculateAcceptChance(a2));
        assertEquals(1.0, accepter.calculateAcceptChance(a3));
        // TODO reable a thorough test of great deluge
//        accepter.stepTaken(stepScope);
//        assertEquals(0.0, accepter.calculateAcceptChance(b1));
//        assertEquals(1.0, accepter.calculateAcceptChance(b2));
//        accepter.stepTaken(stepScope);
//        assertEquals(0.0, accepter.calculateAcceptChance(c1));
//        accepter.stepTaken(stepScope);
//        assertEquals(1.0, accepter.calculateAcceptChance(c2));
//        accepter.stepTaken(stepScope);
//        // Post conditions
//        accepter.solvingEnded(localSearchSolverScope);
    }

    private LocalSearchSolverScope createLocalSearchSolverScope() {
        LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope();
        localSearchSolverScope.setWorkingRandom(new Random() {
            public double nextDouble() {
                return 0.2;
            }
        });
        localSearchSolverScope.setBestScore(-1000.0);
        StepScope lastStepScope = new StepScope(localSearchSolverScope);
        lastStepScope.setScore(-1000.0);
        localSearchSolverScope.setLastCompletedStepScope(lastStepScope);
        return localSearchSolverScope;
    }

    public MoveScope createMoveScope(StepScope stepScope, double score) {
        MoveScope moveScope = new MoveScope(stepScope);
        moveScope.setMove(new DummyMove());
        moveScope.setScore(score);
        return moveScope;
    }

}