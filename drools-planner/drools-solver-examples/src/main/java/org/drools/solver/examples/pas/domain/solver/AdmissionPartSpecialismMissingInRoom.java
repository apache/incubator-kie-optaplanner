package org.drools.solver.examples.pas.domain.solver;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.pas.domain.AdmissionPart;
import org.drools.solver.examples.pas.domain.Room;

/**
 * Calculated during initialization, not modified during score calculation.
 * @author Geoffrey De Smet
 */
@Deprecated
public class AdmissionPartSpecialismMissingInRoom implements Serializable {

    private AdmissionPart admissionPart;
    private Room room;
    private int weight;

    public AdmissionPartSpecialismMissingInRoom(AdmissionPart admissionPart, Room room, int weight) {
        this.admissionPart = admissionPart;
        this.room = room;
        this.weight = weight;
    }

    public AdmissionPart getAdmissionPart() {
        return admissionPart;
    }

    public void setAdmissionPart(AdmissionPart admissionPart) {
        this.admissionPart = admissionPart;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int compareTo(AdmissionPartSpecialismMissingInRoom other) {
        return new CompareToBuilder()
                .append(admissionPart, other.admissionPart)
                .append(room, other.room)
                .toComparison();
    }

    @Override
    public String toString() {
        return admissionPart + " & " + room + " = " + weight;
    }

}