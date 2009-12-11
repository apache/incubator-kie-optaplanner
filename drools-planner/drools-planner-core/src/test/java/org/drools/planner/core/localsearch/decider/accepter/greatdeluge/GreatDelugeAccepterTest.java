package org.drools.planner.core.localsearch.decider.accepter.greatdeluge;

import java.util.Random;

import junit.framework.TestCase;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.localsearch.decider.accepter.Accepter;
import org.drools.planner.core.move.DummyMove;
import org.drools.planner.core.score.DefaultSimpleScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.definition.SimpleScoreDefinition;

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
        MoveScope a1 = createMoveScope(stepScope, DefaultSimpleScore.valueOf(-2000));
        MoveScope a2 = createMoveScope(stepScope, DefaultSimpleScore.valueOf(-1300));
        MoveScope a3 = createMoveScope(stepScope, DefaultSimpleScore.valueOf(-1200));
        MoveScope b1 = createMoveScope(stepScope, DefaultSimpleScore.valueOf(-1200));
        MoveScope b2 = createMoveScope(stepScope, DefaultSimpleScore.valueOf(-100));
        MoveScope c1 = createMoveScope(stepScope, DefaultSimpleScore.valueOf(-1100));
        MoveScope c2 = createMoveScope(stepScope, DefaultSimpleScore.valueOf(-120));
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
        localSearchSolverScope.setScoreDefinition(new SimpleScoreDefinition());
        localSearchSolverScope.setWorkingRandom(new Random() {
            public double nextDouble() {
                return 0.2;
            }
        });
        localSearchSolverScope.setBestScore(DefaultSimpleScore.valueOf(-1000));
        StepScope lastStepScope = new StepScope(localSearchSolverScope);
        lastStepScope.setScore(DefaultSimpleScore.valueOf(-1000));
        localSearchSolverScope.setLastCompletedStepScope(lastStepScope);
        return localSearchSolverScope;
    }

    public MoveScope createMoveScope(StepScope stepScope, Score score) {
        MoveScope moveScope = new MoveScope(stepScope);
        moveScope.setMove(new DummyMove());
        moveScope.setScore(score);
        return moveScope;
    }

}