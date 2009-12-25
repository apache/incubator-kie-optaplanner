package org.drools.planner.examples.pas.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("RoomSpecialism")
public class RoomSpecialism extends AbstractPersistable implements Comparable<RoomSpecialism> {

    private Room room;
    private Specialism specialism;

    private int priority; // AKA choice

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Specialism getSpecialism() {
        return specialism;
    }

    public void setSpecialism(Specialism specialism) {
        this.specialism = specialism;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int compareTo(RoomSpecialism other) {
        return new CompareToBuilder()
                .append(room, other.room)
                .append(specialism, other.specialism)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return room + "-" + specialism;
    }

}