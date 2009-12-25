package org.drools.planner.examples.pas.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * AKA RoomProperty
 * @author Geoffrey De Smet
 */
@XStreamAlias("Equipment")
public class Equipment extends AbstractPersistable implements Comparable<Equipment> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(Equipment other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return name;
    }

}