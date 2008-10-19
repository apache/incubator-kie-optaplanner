package org.drools.solver.core.score;

import junit.framework.TestCase;

/**
 * @author Geoffrey De Smet
 */
public class HardAndSoftScoreTest extends TestCase {

    public void testCompareTo() {
        Score a = new HardAndSoftScore(-1, -300);
        Score b = new HardAndSoftScore(-20, -20);
        Score c = new HardAndSoftScore(-20);
        assertTrue(a.compareTo(b) > 0);
        assertTrue(b.compareTo(a) < 0);
        assertTrue(b.compareTo(c) > 0);
        assertTrue(c.compareTo(b) < 0);
    }

}
