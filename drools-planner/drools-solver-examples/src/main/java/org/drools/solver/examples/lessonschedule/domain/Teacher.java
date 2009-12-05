package org.drools.solver.examples.lessonschedule.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Teacher extends AbstractPersistable implements Comparable<Teacher> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(Teacher other) {
        return new CompareToBuilder()
                .append(name, other.name)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return getName();
    }

}
