package org.drools.solver.examples.nqueens.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Queen extends AbstractPersistable implements Comparable<Queen> {

    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getAscendingD() {
        return (x + y);
    }

    public int getDescendingD() {
        return (x - y);
    }


    public int compareTo(Queen other) {
        return new CompareToBuilder()
                .append(x, other.x)
                .append(y, other.y)
                .append(id, other.id)
                .toComparison();
    }

    public Queen clone() {
        Queen clone = new Queen();
        clone.id = id;
        clone.x = x;
        clone.y = y;
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() + " " + x + " @ " + y;
    }
    
}
