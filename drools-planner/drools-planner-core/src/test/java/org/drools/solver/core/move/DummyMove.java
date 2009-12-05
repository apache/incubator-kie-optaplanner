package org.drools.solver.core.move;

import org.drools.WorkingMemory;

/**
 * @author Geoffrey De Smet
*/
public class DummyMove implements Move {

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return true;
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new DummyMove();
    }

    public void doMove(WorkingMemory workingMemory) {
        // do nothing
    }

}
