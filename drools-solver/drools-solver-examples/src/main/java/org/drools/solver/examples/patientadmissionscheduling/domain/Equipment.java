package org.drools.solver.examples.patientadmissionscheduling.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * AKA roomproperty
 * @author Geoffrey De Smet
 */
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