package org.drools.solver.core.localsearch.decider.forager;

import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import org.drools.solver.core.localsearch.DefaultLocalSearchSolver;
import org.drools.solver.core.move.DummyMove;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class FirstRandomlyAcceptedForagerTest extends TestCase {

    public void testPicking() {
        Forager forager = new FirstRandomlyAcceptedForager();
        forager.setLocalSearchSolver(new DefaultLocalSearchSolver() {
            public Random getRandom() {
                return new Random() {
                    public double nextDouble() {
                        return 0.99;
                    }
                };
            }
        });
        forager.solvingStarted();
        forager.beforeDeciding();
        Move a = new DummyMove();
        Move b = new DummyMove();
        Move c = new DummyMove();
        Move d = new DummyMove();
        forager.addMove(a, -100.0, 0.0);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b, -10.0, 0.8);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c, -10.0, 0.0);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d, -100.0, 1.0);
        assertTrue(forager.isQuitEarly());
        Move picked = forager.pickMove();
        assertTrue(picked == d);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b));
        assertTrue(topList.contains(d));
        forager.solvingEnded();
    }

}