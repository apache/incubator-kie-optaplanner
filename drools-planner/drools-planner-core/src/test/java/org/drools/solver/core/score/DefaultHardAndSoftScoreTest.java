package org.drools.solver.core.score;

import junit.framework.TestCase;

/**
 * @author Geoffrey De Smet
 */
public class DefaultHardAndSoftScoreTest extends TestCase {

    public void testCompareTo() {
        Score a = new DefaultHardAndSoftScore(-1, -300);
        Score b = new DefaultHardAndSoftScore(-20, -20);
        Score c = new DefaultHardAndSoftScore(-20);
        assertTrue(a.compareTo(b) > 0);
        assertTrue(b.compareTo(a) < 0);
        assertTrue(b.compareTo(c) > 0);
        assertTrue(c.compareTo(b) < 0);
    }

}
