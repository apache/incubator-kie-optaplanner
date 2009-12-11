package org.drools.planner.core.move;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import org.drools.WorkingMemory;

/**
 * A CompositeMove is composided out of multiple other moves.
 * <p/>
 * Warning: one of the moveList moves should not rely on the effect on of a previous moveList move
 * to create an uncorrupted undoMove. In other words, 
 * @see Move
 * @author Geoffrey De Smet
 */
public class CompositeMove implements Move {

    protected List<Move> moveList;

    public List<Move> getMoveList() {
        return moveList;
    }

    /**
     * @param moveList cannot be null
     */
    public CompositeMove(List<Move> moveList) {
        this.moveList = moveList;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        for (Move move : moveList) {
            if (!move.isMoveDoable(workingMemory)) {
                return false;
            }
        }
        return true;
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        List<Move> undoMoveList = new ArrayList<Move>(moveList.size());
        for (Move move : moveList) {
            // Note: this undoMove doesn't have the affect of a previous move in the moveList
            // This could be made possible by merging the methods createUndoMove and doMove...
            Move undoMove = move.createUndoMove(workingMemory);
            undoMoveList.add(undoMove);
        }
        Collections.reverse(undoMoveList);
        return new CompositeMove(undoMoveList);
    }

    public void doMove(WorkingMemory workingMemory) {
        for (Move move : moveList) {
            move.doMove(workingMemory);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof CompositeMove) {
            CompositeMove other = (CompositeMove) o;
            return moveList.equals(other.moveList);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return moveList.hashCode();
    }

    public String toString() {
        return moveList.toString();
    }

}
