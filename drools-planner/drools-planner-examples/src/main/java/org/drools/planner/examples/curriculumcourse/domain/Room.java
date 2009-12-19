package org.drools.planner.examples.curriculumcourse.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Room")
public class Room extends AbstractPersistable implements Comparable<Room> {

    private String code;
    private int capacity;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int compareTo(Room other) {
        return new CompareToBuilder()
                .append(code, other.code)
                .toComparison();
    }

    @Override
    public String toString() {
        return code + " {C" + capacity + "}";
    }

}