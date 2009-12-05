package org.drools.solver.examples.manners2009.domain;

import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Table extends AbstractPersistable implements Comparable<Table> {

    private int tableIndex;

    private List<Seat> seatList;

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public List<Seat> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }

    public int compareTo(Table other) {
        return new CompareToBuilder()
                .append(tableIndex, other.tableIndex)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return Integer.toString(tableIndex);
    }

}