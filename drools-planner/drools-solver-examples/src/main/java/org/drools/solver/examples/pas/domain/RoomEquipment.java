package org.drools.solver.examples.pas.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class RoomEquipment extends AbstractPersistable implements Comparable<RoomEquipment> {

    private Room room;
    private Equipment equipment;

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public int compareTo(RoomEquipment other) {
        return new CompareToBuilder()
                .append(room, other.room)
                .append(equipment, other.equipment)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return room + "-" + equipment;
    }

}