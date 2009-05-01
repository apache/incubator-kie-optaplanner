package org.drools.solver.examples.manners2009.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class Table extends AbstractPersistable implements Comparable<Table> {

    private int tableIndex;
    private int size;

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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