package org.drools.solver.examples.patientadmissionschedule.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.common.domain.AbstractPersistable;
import org.drools.solver.examples.itc2007.examination.domain.solver.TopicConflict;
import org.drools.solver.examples.itc2007.examination.domain.Topic;
import org.drools.solver.examples.itc2007.examination.domain.Student;
import org.drools.solver.examples.patientadmissionschedule.domain.solver.AdmissionPartConflict;
import org.drools.solver.examples.patientadmissionschedule.domain.solver.AdmissionPartSpecialismMissingInRoom;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionSchedule extends AbstractPersistable implements Solution {

    private List<Specialism> specialismList;
    private List<Equipment> equipmentList;
    private List<Department> departmentList;
    private List<DepartmentSpecialism> departmentSpecialismList;
    private List<Room> roomList;
    private List<RoomSpecialism> roomSpecialismList;
    private List<RoomEquipment> roomEquipmentList;
    private List<Bed> bedList;
    private List<Night> nightList;
    private List<Patient> patientList;
    private List<AdmissionPart> admissionPartList;
    private List<RequiredPatientEquipment> requiredPatientEquipmentList;
    private List<PreferredPatientEquipment> preferredPatientEquipmentList;

    private List<BedDesignation> bedDesignationList;

    public List<Specialism> getSpecialismList() {
        return specialismList;
    }

    public void setSpecialismList(List<Specialism> specialismList) {
        this.specialismList = specialismList;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public List<Department> getDepartmentList() {
        return departmentList;
    }

    public void setDepartmentList(List<Department> departmentList) {
        this.departmentList = departmentList;
    }

    public List<DepartmentSpecialism> getDepartmentSpecialismList() {
        return departmentSpecialismList;
    }

    public void setDepartmentSpecialismList(List<DepartmentSpecialism> departmentSpecialismList) {
        this.departmentSpecialismList = departmentSpecialismList;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
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

    public List<Bed> getBedList() {
        return bedList;
    }

    public void setBedList(List<Bed> bedList) {
        this.bedList = bedList;
    }

    public List<Night> getNightList() {
        return nightList;
    }

    public void setNightList(List<Night> nightList) {
        this.nightList = nightList;
    }

    public List<Patient> getPatientList() {
        return patientList;
    }

    public void setPatientList(List<Patient> patientList) {
        this.patientList = patientList;
    }

    public List<AdmissionPart> getAdmissionPartList() {
        return admissionPartList;
    }

    public void setAdmissionPartList(List<AdmissionPart> admissionPartList) {
        this.admissionPartList = admissionPartList;
    }

    public List<RequiredPatientEquipment> getRequiredPatientEquipmentList() {
        return requiredPatientEquipmentList;
    }

    public void setRequiredPatientEquipmentList(List<RequiredPatientEquipment> requiredPatientEquipmentList) {
        this.requiredPatientEquipmentList = requiredPatientEquipmentList;
    }

    public List<PreferredPatientEquipment> getPreferredPatientEquipmentList() {
        return preferredPatientEquipmentList;
    }

    public void setPreferredPatientEquipmentList(List<PreferredPatientEquipment> preferredPatientEquipmentList) {
        this.preferredPatientEquipmentList = preferredPatientEquipmentList;
    }

    public List<BedDesignation> getBedDesignationList() {
        return bedDesignationList;
    }

    public void setBedDesignationList(List<BedDesignation> bedDesignationList) {
        this.bedDesignationList = bedDesignationList;
    }

    public boolean isInitialized() {
        return (bedDesignationList != null);
    }

    public Collection<? extends Object> getFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(specialismList);
        facts.addAll(equipmentList);
        facts.addAll(departmentList);
        facts.addAll(departmentSpecialismList);
        facts.addAll(roomList);
        facts.addAll(roomSpecialismList);
        facts.addAll(roomEquipmentList);
        facts.addAll(bedList);
        facts.addAll(nightList);
        facts.addAll(patientList);
        facts.addAll(admissionPartList);
        facts.addAll(requiredPatientEquipmentList);
        facts.addAll(preferredPatientEquipmentList);
        if (isInitialized()) {
            facts.addAll(bedDesignationList);
        }
        facts.addAll(calculateAdmissionPartConflictList());
        facts.addAll(calculateAdmissionPartSpecialismMissingInRoomList());
        return facts;
    }

    private List<AdmissionPartConflict> calculateAdmissionPartConflictList() {
        List<AdmissionPartConflict> admissionPartConflictList = new ArrayList<AdmissionPartConflict>();
        for (AdmissionPart leftAdmissionPart : admissionPartList) {
            for (AdmissionPart rightAdmissionPart : admissionPartList) {
                if (leftAdmissionPart.getId() < rightAdmissionPart.getId()) {
                    int sameNightCount = leftAdmissionPart.calculateSameNightCount(rightAdmissionPart);
                    if (sameNightCount > 0) {
                        admissionPartConflictList.add(new AdmissionPartConflict(
                                leftAdmissionPart, rightAdmissionPart, sameNightCount));
                    }
                }
            }
        }
        return admissionPartConflictList;
    }

    private List<AdmissionPartSpecialismMissingInRoom> calculateAdmissionPartSpecialismMissingInRoomList() {
        List<AdmissionPartSpecialismMissingInRoom> admissionPartSpecialismMissingInRoomList
                = new ArrayList<AdmissionPartSpecialismMissingInRoom>();
        for (AdmissionPart admissionPart : admissionPartList) {
            if (admissionPart.getSpecialism() != null) {
                for (Room room : roomList) {
                    int mininumPriority = Integer.MAX_VALUE;
                    for (RoomSpecialism roomSpecialism : room.getRoomSpecialismList()) {
                        if (roomSpecialism.getSpecialism().equals(admissionPart.getSpecialism())) {
                            mininumPriority = Math.min(mininumPriority, roomSpecialism.getPriority());
                        }
                    }
                    int weight = (mininumPriority == Integer.MAX_VALUE) ? 2 : mininumPriority - 1;
                    if (weight > 0) {
                        admissionPartSpecialismMissingInRoomList.add(
                                new AdmissionPartSpecialismMissingInRoom(admissionPart, room, mininumPriority));
                    }
                }
            }
        }
        return admissionPartSpecialismMissingInRoomList;
    }

    /**
     * Clone will only deep copy the bedDesignationList
     */
    public PatientAdmissionSchedule cloneSolution() {
        PatientAdmissionSchedule clone = new PatientAdmissionSchedule();
        clone.id = id;
        clone.specialismList = specialismList;
        clone.equipmentList = equipmentList;
        clone.departmentList = departmentList;
        clone.departmentSpecialismList = departmentSpecialismList;
        clone.roomList = roomList;
        clone.roomSpecialismList = roomSpecialismList;
        clone.roomEquipmentList = roomEquipmentList;
        clone.bedList = bedList;
        clone.nightList = nightList;
        clone.patientList = patientList;
        clone.admissionPartList = admissionPartList;
        clone.requiredPatientEquipmentList = requiredPatientEquipmentList;
        clone.preferredPatientEquipmentList = preferredPatientEquipmentList;
        List<BedDesignation> clonedBedDesignationList = new ArrayList<BedDesignation>(bedDesignationList.size());
        for (BedDesignation bedDesignation : bedDesignationList) {
            BedDesignation clonedBedDesignation = bedDesignation.clone();
            clonedBedDesignationList.add(clonedBedDesignation);
        }
        clone.bedDesignationList = clonedBedDesignationList;
        return clone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (id == null || !(o instanceof PatientAdmissionSchedule)) {
            return false;
        } else {
            PatientAdmissionSchedule other = (PatientAdmissionSchedule) o;
            if (bedDesignationList.size() != other.bedDesignationList.size()) {
                return false;
            }
            for (Iterator<BedDesignation> it = bedDesignationList.iterator(), otherIt = other.bedDesignationList.iterator(); it.hasNext();) {
                BedDesignation bedDesignation = it.next();
                BedDesignation otherBedDesignation = otherIt.next();
                // Notice: we don't use equals()
                if (!bedDesignation.solutionEquals(otherBedDesignation)) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (BedDesignation bedDesignation : bedDesignationList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(bedDesignation.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }

}