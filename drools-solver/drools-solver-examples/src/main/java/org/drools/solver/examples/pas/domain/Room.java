package org.drools.solver.examples.pas.domain;

import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Room extends AbstractPersistable implements Comparable<Room> {

    private String name;

    private Department department;
    private int capacity;
    private GenderLimitation genderLimitation;

    private List<RoomSpecialism> roomSpecialismList;
    private List<RoomEquipment> roomEquipmentList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public GenderLimitation getGenderLimitation() {
        return genderLimitation;
    }

    public void setGenderLimitation(GenderLimitation genderLimitation) {
        this.genderLimitation = genderLimitation;
    }

    public List<RoomSpecialism> getRoomSpecialismList() {
        return roomSpecialismList;
    }

    public void setRoomSpecialismList(List<RoomSpecialism> roomSpecialismList) {
        this.roomSpecialismList = roomSpecialismList;
    }

    public List<RoomEquipment> getRoomEquipmentList() {
        return roomEquipmentList;
    }

    public void setRoomEquipmentList(List<RoomEquipment> roomEquipmentList) {
        this.roomEquipmentList = roomEquipmentList;
    }

    public int compareTo(Room other) {
        return new CompareToBuilder()
                .append(department, other.department)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return department + "_" + name;
    }

    public boolean allowsAdmissionPart(AdmissionPart admissionPart) {
        return department.allowsAdmissionPart(admissionPart)
            && allowsPatientGender(admissionPart.getPatient())
            && hasRequiredRoomProperties(admissionPart.getPatient());
    }

    public boolean allowsPatientGender(Patient patient) {
        switch (genderLimitation) {
            case ANY_GENDER:
                return true;
            case MALE_ONLY:
                return patient.getGender() == Gender.MALE;
            case FEMALE_ONLY:
                return patient.getGender() == Gender.FEMALE;
            case SAME_GENDER:
                // scoreRules check this
                return true;
            default:
                throw new IllegalStateException("The genderLimitation (" + genderLimitation + ") is not implemented");
        }
    }

    public boolean hasRequiredRoomProperties(Patient patient) {
        for (RequiredPatientEquipment requiredPatientEquipment : patient.getRequiredPatientEquipmentList()) {
            Equipment requiredEquipment = requiredPatientEquipment.getEquipment();
            boolean hasRequiredEquipment = false;
            for (RoomEquipment roomEquipment : roomEquipmentList) {
                if (roomEquipment.getEquipment().equals(requiredEquipment)) {
                    hasRequiredEquipment = true;
                }
            }
            if (!hasRequiredEquipment) {
                return false;
            }
        }
        return true;
    }

    public int countDisallowedAdmissionPart(AdmissionPart admissionPart) {
        return department.countDisallowedAdmissionPart(admissionPart)
            + countDisallowedPatientGender(admissionPart.getPatient())
            + countMissingRequiredRoomProperties(admissionPart.getPatient());
    }

    public int countDisallowedPatientGender(Patient patient) {
        switch (genderLimitation) {
            case ANY_GENDER:
                return 0;
            case MALE_ONLY:
                return patient.getGender() == Gender.MALE ? 0 : 1;
            case FEMALE_ONLY:
                return patient.getGender() == Gender.FEMALE ? 0 : 1;
            case SAME_GENDER:
                // scoreRules check this
                return 0;
            default:
                throw new IllegalStateException("The genderLimitation (" + genderLimitation + ") is not implemented");
        }
    }

    public int countMissingRequiredRoomProperties(Patient patient) {
        int count = 0;
        for (RequiredPatientEquipment requiredPatientEquipment : patient.getRequiredPatientEquipmentList()) {
            Equipment requiredEquipment = requiredPatientEquipment.getEquipment();
            boolean hasRequiredEquipment = false;
            for (RoomEquipment roomEquipment : roomEquipmentList) {
                if (roomEquipment.getEquipment().equals(requiredEquipment)) {
                    hasRequiredEquipment = true;
                }
            }
            if (!hasRequiredEquipment) {
                count++;
            }
        }
        return count;
    }

}