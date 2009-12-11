package org.drools.planner.examples.manners2009.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Seat extends AbstractPersistable implements Comparable<Seat> {

    private Table table;
    private int seatIndexInTable;

    private Seat leftSeat;
    private Seat rightSeat;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public int getSeatIndexInTable() {
        return seatIndexInTable;
    }

    public void setSeatIndexInTable(int seatIndexInTable) {
        this.seatIndexInTable = seatIndexInTable;
    }

    public Seat getLeftSeat() {
        return leftSeat;
    }

    public void setLeftSeat(Seat leftSeat) {
        this.leftSeat = leftSeat;
    }

    public Seat getRightSeat() {
        return rightSeat;
    }

    public void setRightSeat(Seat rightSeat) {
        this.rightSeat = rightSeat;
    }

    public int compareTo(Seat other) {
        return new CompareToBuilder()
                .append(table, other.table)
                .append(seatIndexInTable, other.seatIndexInTable)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return table + "." + seatIndexInTable;
    }

    public Gender getRequiredGender() {
        return (seatIndexInTable % 2 == 0) ? Gender.MALE : Gender.FEMALE;
    }

}