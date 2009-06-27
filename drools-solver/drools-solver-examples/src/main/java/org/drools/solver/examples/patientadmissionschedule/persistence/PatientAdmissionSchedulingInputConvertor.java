package org.drools.solver.examples.patientadmissionschedule.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.drools.solver.examples.common.persistence.AbstractInputConvertor;
import org.drools.solver.examples.patientadmissionschedule.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.patientadmissionschedule.domain.Specialism;
import org.drools.solver.examples.patientadmissionschedule.domain.Department;
import org.drools.solver.examples.patientadmissionschedule.domain.DepartmentSpecialism;
import org.drools.solver.examples.patientadmissionschedule.domain.Equipment;
import org.drools.solver.examples.patientadmissionschedule.domain.Room;
import org.drools.solver.examples.patientadmissionschedule.domain.RoomSpecialism;
import org.drools.solver.examples.patientadmissionschedule.domain.GenderLimitation;
import org.drools.solver.examples.patientadmissionschedule.domain.RoomEquipment;
import org.drools.solver.examples.patientadmissionschedule.domain.Bed;
import org.drools.solver.examples.patientadmissionschedule.domain.Night;
import org.drools.solver.examples.patientadmissionschedule.domain.Patient;
import org.drools.solver.examples.patientadmissionschedule.domain.Admission;
import org.drools.solver.examples.patientadmissionschedule.domain.RequiredPatientEquipment;
import org.drools.solver.examples.patientadmissionschedule.domain.PreferredPatientEquipment;
import org.drools.solver.examples.patientadmissionschedule.domain.Gender;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionSchedulingInputConvertor extends AbstractInputConvertor {

    public static void main(String[] args) {
        new PatientAdmissionSchedulingInputConvertor().convertAll();
    }

    protected String getExampleDirName() {
        return "patientadmissionschedule";
    }

    public InputBuilder createInputBuilder() {
        return new PatientAdmissionSchedulingInputBuilder();
    }

    public class PatientAdmissionSchedulingInputBuilder extends InputBuilder {

        private PatientAdmissionSchedule patientAdmissionSchedule;

        private int specialismListSize;
        private int departmentListSize;
        private int equipmentListSize;
        private int roomListSize;
        private int bedListSize;
        private int nightListSize;
        private int patientListSize;

        private Map<Long, Specialism> idToSpecialismMap = null;
        private Map<Long, Department> idToDepartmentMap = null;
        private Map<Integer, Equipment> indexToEquipmentMap = null;
        private Map<Long, Room> idToRoomMap = null;
        private Map<Integer, Night> indexToNightMap = null;

        public Solution readSolution() throws IOException {
            patientAdmissionSchedule = new PatientAdmissionSchedule();
            patientAdmissionSchedule.setId(0L);
            readSizes();
            readEmptyLine();
            readEmptyLine();
            readSpecialismList();
            readEmptyLine();
            readDepartmentListAndDepartmentSpecialismList();
            readEmptyLine();
            readEquipmentList();
            readEmptyLine();
            readRoomListAndRoomSpecialismListAndRoomEquipmentList();
            readEmptyLine();
            readBedList();
            readEmptyLine();
            generateNightList();
            readPatientListAndAdmissionListAndRequiredPatientEquipmentListAndPreferredPatientEquipmentList();
            readEmptyLine();
            readConstantLine("END.");


            return patientAdmissionSchedule;
        }

        private void readSizes() throws IOException {
            readConstantLine("ARTICLE BENCHMARK DATA SET");
            roomListSize = readIntegerValue("Rooms:");
            equipmentListSize = readIntegerValue("Roomproperties:");
            bedListSize = readIntegerValue("Beds:");
            departmentListSize = readIntegerValue("Departments:");
            specialismListSize = readIntegerValue("Specialisms:");
            patientListSize = readIntegerValue("Patients:");
            nightListSize = readIntegerValue("Planning horizon:");
        }

        private void readSpecialismList() throws IOException {
            readConstantLine("SPECIALISMS:");
            List<Specialism> specialismList = new ArrayList<Specialism>(specialismListSize);
            idToSpecialismMap = new HashMap<Long, Specialism>(specialismListSize);
            for (int i = 0; i < specialismListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpace(line, 2);
                Specialism specialism = new Specialism();
                specialism.setId(Long.parseLong(lineTokens[0]));
                specialism.setName(lineTokens[1]);
                specialismList.add(specialism);
                idToSpecialismMap.put(specialism.getId(), specialism);
            }
            patientAdmissionSchedule.setSpecialismList(specialismList);
        }

        private void readDepartmentListAndDepartmentSpecialismList() throws IOException {
            readConstantLine("DEPARTMENTS:");
            List<Department> departmentList = new ArrayList<Department>(departmentListSize);
            idToDepartmentMap = new HashMap<Long, Department>(departmentListSize);
            List<DepartmentSpecialism> departmentSpecialismList = new ArrayList<DepartmentSpecialism>(
                    departmentListSize * 5);
            long departmentSpecialismId = 0L;
            for (int i = 0; i < departmentListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitByPipeline(line, 2);

                String[] departmentTokens = splitBySpace(lineTokens[0], 4);
                Department department = new Department();
                department.setId(Long.parseLong(departmentTokens[0]));
                department.setName(departmentTokens[1]);
                int minimumAge = Integer.parseInt(departmentTokens[2]);
                if (minimumAge != 0) {
                    department.setMinimumAge(Integer.valueOf(minimumAge));
                }
                int maximumAge = Integer.parseInt(departmentTokens[3]);
                if (maximumAge != 0) {
                    department.setMaximumAge(Integer.valueOf(maximumAge));
                }
                departmentList.add(department);
                idToDepartmentMap.put(department.getId(), department);

                String[] departmentSpecialismTokens = splitBySpace(lineTokens[1]);
                if (departmentSpecialismTokens.length % 2 != 0) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain even number of tokens (" + departmentSpecialismTokens.length
                            + ") after 1st pipeline (|) seperated by a space ( ).");
                }
                for (int j = 0; j < departmentSpecialismTokens.length; j += 2) {
                    DepartmentSpecialism departmentSpecialism = new DepartmentSpecialism();
                    departmentSpecialism.setId(departmentSpecialismId);
                    departmentSpecialism.setDepartment(department);
                    departmentSpecialism.setPriority(Integer.parseInt(departmentSpecialismTokens[j]));
                    departmentSpecialism.setSpecialism(idToSpecialismMap.get(
                            Long.parseLong(departmentSpecialismTokens[j + 1])));
                    departmentSpecialismList.add(departmentSpecialism);
                    departmentSpecialismId++;
                }
            }
            patientAdmissionSchedule.setDepartmentList(departmentList);
            patientAdmissionSchedule.setDepartmentSpecialismList(departmentSpecialismList);
        }

        private void readEquipmentList() throws IOException {
            readConstantLine("ROOMPROPERTIES:");
            List<Equipment> equipmentList = new ArrayList<Equipment>(equipmentListSize);
            indexToEquipmentMap = new HashMap<Integer, Equipment>(equipmentListSize);
            for (int i = 0; i < equipmentListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpace(line, 2);
                Equipment equipment = new Equipment();
                equipment.setId(Long.parseLong(lineTokens[0]));
                equipment.setName(lineTokens[1]);
                equipmentList.add(equipment);
                indexToEquipmentMap.put(i, equipment);
            }
            patientAdmissionSchedule.setEquipmentList(equipmentList);
        }

        private void readRoomListAndRoomSpecialismListAndRoomEquipmentList() throws IOException {
            readConstantLine("ROOMS:");
            List<Room> roomList = new ArrayList<Room>(roomListSize);
            idToRoomMap = new HashMap<Long, Room>(roomListSize);
            List<RoomSpecialism> roomSpecialismList = new ArrayList<RoomSpecialism>(roomListSize * 5);
            List<RoomEquipment> roomEquipmentList = new ArrayList<RoomEquipment>(roomListSize * 2);
            long roomSpecialismId = 0L;
            long roomEquipmentId = 0L;
            for (int i = 0; i < roomListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitByPipeline(line, 6);

                String[] roomTokens = splitBySpace(lineTokens[0], 2);
                Room room = new Room();
                room.setId(Long.parseLong(roomTokens[0]));
                room.setName(roomTokens[1]);
                room.setCapacity(Integer.parseInt(lineTokens[1]));
                room.setDepartment(idToDepartmentMap.get(
                        Long.parseLong(lineTokens[2])));
                room.setGenderLimitation(GenderLimitation.valueOfCode(lineTokens[3]));
                roomList.add(room);
                idToRoomMap.put(room.getId(), room);

                String[] roomSpecialismTokens = splitBySpace(lineTokens[4]);
                if (roomSpecialismTokens.length % 2 != 0) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain even number of tokens (" + roomSpecialismTokens.length
                            + ") after 4th pipeline (|) seperated by a space ( ).");
                }
                for (int j = 0; j < roomSpecialismTokens.length; j += 2) {
                    int priority = Integer.parseInt(roomSpecialismTokens[j]);
                    long specialismId = Long.parseLong(roomSpecialismTokens[j + 1]);
                    RoomSpecialism roomSpecialism = new RoomSpecialism();
                    roomSpecialism.setId(roomSpecialismId);
                    roomSpecialism.setRoom(room);
                    roomSpecialism.setSpecialism(idToSpecialismMap.get(specialismId));
                    roomSpecialism.setPriority(priority);
                    roomSpecialismList.add(roomSpecialism);
                    roomSpecialismId++;
                }

                String[] roomEquipmentTokens = splitBySpace(lineTokens[5]);
                if (roomEquipmentTokens.length != equipmentListSize) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain equal number of tokens (" + roomEquipmentTokens.length
                            + ") as equipmentListSize (" + equipmentListSize + ") after 5th pipeline (|).");
                }
                for (int j = 0; j < roomEquipmentTokens.length; j++) {
                    int hasEquipment = Integer.parseInt(roomEquipmentTokens[j]);
                    if (hasEquipment == 1) {
                        RoomEquipment roomEquipment = new RoomEquipment();
                        roomEquipment.setId(roomEquipmentId);
                        roomEquipment.setRoom(room);
                        roomEquipment.setEquipment(indexToEquipmentMap.get(j));
                        roomEquipmentList.add(roomEquipment);
                        roomEquipmentId++;
                    } else if (hasEquipment != 0) {
                        throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to have 0 or 1 hasEquipment (" + hasEquipment + ").");
                    }
                }
            }
            patientAdmissionSchedule.setRoomList(roomList);
            patientAdmissionSchedule.setRoomSpecialismList(roomSpecialismList);
            patientAdmissionSchedule.setRoomEquipmentList(roomEquipmentList);
        }

        private void readBedList() throws IOException {
            readConstantLine("BEDS:");
            List<Bed> bedList = new ArrayList<Bed>(bedListSize);
            Map<Room, Integer> roomToLastIndexInRoomMap = new HashMap<Room, Integer>(roomListSize);
            for (int i = 0; i < bedListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpace(line, 2);
                Bed bed = new Bed();
                bed.setId(Long.parseLong(lineTokens[0]));
                Room room = idToRoomMap.get(Long.parseLong(lineTokens[1]));
                bed.setRoom(room);
                Integer indexInRoom = roomToLastIndexInRoomMap.get(room);
                if (indexInRoom == null) {
                    indexInRoom = 0;
                } else {
                    indexInRoom++;
                }
                bed.setIndexInRoom(indexInRoom);
                roomToLastIndexInRoomMap.put(room, indexInRoom);
                bedList.add(bed);
            }
            patientAdmissionSchedule.setBedList(bedList);
        }

        private void generateNightList() {
            List<Night> nightList = new ArrayList<Night>(nightListSize);
            indexToNightMap = new HashMap<Integer, Night>(nightListSize);
            long nightId = 0L;
            for (int i = 0; i < nightListSize; i++) {
                Night night = new Night();
                night.setId(nightId);
                night.setIndex(i);
                nightList.add(night);
                indexToNightMap.put(i, night);
                nightId++;
            }
            patientAdmissionSchedule.setNightList(nightList);
        }

        private void readPatientListAndAdmissionListAndRequiredPatientEquipmentListAndPreferredPatientEquipmentList() throws IOException {
            readConstantLine("PATIENTS:");
            List<Patient> patientList = new ArrayList<Patient>(patientListSize);
            List<Admission> admissionList = new ArrayList<Admission>(patientListSize);
            List<RequiredPatientEquipment> requiredPatientEquipmentList = new ArrayList<RequiredPatientEquipment>(patientListSize * equipmentListSize);
            List<PreferredPatientEquipment> preferredPatientEquipmentList = new ArrayList<PreferredPatientEquipment>(patientListSize * equipmentListSize);
            long admissionId = 0L;
            long requiredPatientEquipmentId = 0L;
            long preferredPatientEquipmentId = 0L;
            for (int i = 0; i < patientListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitByPipeline(line, 6);

                String[] patientTokens = splitBySpace(lineTokens[0], 4);
                Patient patient = new Patient();
                patient.setId(Long.parseLong(patientTokens[0]));
                patient.setName(patientTokens[1]);
                patient.setAge(Integer.parseInt(patientTokens[2]));
                patient.setGender(Gender.valueOfCode(patientTokens[3]));
                patient.setPreferredMaximumRoomCapacity(Integer.parseInt(lineTokens[3]));
                patientList.add(patient);

                String[] nightTokens = splitBySpace(lineTokens[1], 2);
                Night firstNight = indexToNightMap.get(Integer.parseInt(nightTokens[0]));
                int lastNightIndex = Integer.parseInt(nightTokens[1]);
                ensureEnoughNights(lastNightIndex);
                Night endNight = indexToNightMap.get(lastNightIndex);
                int patientNightListSize = endNight.getIndex() - firstNight.getIndex();

                String[] admissionTokens = splitBySpace(lineTokens[2]);
                if (admissionTokens.length % 2 != 1) {
                }
                int patientAdmissionListSize = Integer.parseInt(admissionTokens[0]);
                if (admissionTokens.length != ((patientAdmissionListSize * 2) + 1)) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain " + ((patientAdmissionListSize * 2) + 1)
                            + " number of tokens after 2th pipeline (|).");
                }
                int nextFirstNightIndex = firstNight.getIndex();
                for (int j = 1; j < admissionTokens.length; j += 2) {
                    long specialismId = Long.parseLong(admissionTokens[j]);
                    int admissionNightListSize = Integer.parseInt(admissionTokens[j + 1]);
                    Admission admission = new Admission();
                    admission.setId(admissionId);
                    admission.setPatient(patient);
                    admission.setSpecialism(idToSpecialismMap.get(specialismId));
                    admission.setFirstNight(indexToNightMap.get(nextFirstNightIndex));
                    admission.setLastNight(indexToNightMap.get(nextFirstNightIndex + admissionNightListSize - 1));
                    admissionList.add(admission);
                    admissionId++;
                    nextFirstNightIndex += admissionNightListSize;
                }
                if (nextFirstNightIndex != nextFirstNightIndex) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") has patientNightListSize (" + patientNightListSize
                            + ") different from the sum of admissionNightListSize (" + nextFirstNightIndex + ")");
                }

                String[] requiredPatientEquipmentTokens = splitBySpace(lineTokens[4]);
                if (requiredPatientEquipmentTokens.length != equipmentListSize) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain equal number of tokens ("
                            + requiredPatientEquipmentTokens.length
                            + ") as equipmentListSize (" + equipmentListSize + ") after 4th pipeline (|).");
                }
                for (int j = 0; j < requiredPatientEquipmentTokens.length; j++) {
                    int hasEquipment = Integer.parseInt(requiredPatientEquipmentTokens[j]);
                    if (hasEquipment == 1) {
                        RequiredPatientEquipment requiredPatientEquipment = new RequiredPatientEquipment();
                        requiredPatientEquipment.setId(requiredPatientEquipmentId);
                        requiredPatientEquipment.setPatient(patient);
                        requiredPatientEquipment.setEquipment(indexToEquipmentMap.get(j));
                        requiredPatientEquipmentList.add(requiredPatientEquipment);
                        requiredPatientEquipmentId++;
                    } else if (hasEquipment != 0) {
                        throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to have 0 or 1 hasEquipment (" + hasEquipment + ").");
                    }
                }

                String[] preferredPatientEquipmentTokens = splitBySpace(lineTokens[5]);
                if (preferredPatientEquipmentTokens.length != equipmentListSize) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain equal number of tokens ("
                            + preferredPatientEquipmentTokens.length
                            + ") as equipmentListSize (" + equipmentListSize + ") after 5th pipeline (|).");
                }
                for (int j = 0; j < preferredPatientEquipmentTokens.length; j++) {
                    int hasEquipment = Integer.parseInt(preferredPatientEquipmentTokens[j]);
                    if (hasEquipment == 1) {
                        PreferredPatientEquipment preferredPatientEquipment = new PreferredPatientEquipment();
                        preferredPatientEquipment.setId(preferredPatientEquipmentId);
                        preferredPatientEquipment.setPatient(patient);
                        preferredPatientEquipment.setEquipment(indexToEquipmentMap.get(j));
                        preferredPatientEquipmentList.add(preferredPatientEquipment);
                        preferredPatientEquipmentId++;
                    } else if (hasEquipment != 0) {
                        throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to have 0 or 1 hasEquipment (" + hasEquipment + ").");
                    }
                }
            }
            patientAdmissionSchedule.setPatientList(patientList);
            patientAdmissionSchedule.setAdmissionList(admissionList);
            patientAdmissionSchedule.setRequiredPatientEquipmentList(requiredPatientEquipmentList);
            patientAdmissionSchedule.setPreferredPatientEquipmentList(preferredPatientEquipmentList);
        }

        /**
         * hack to make sure there are enough nights
         * @param lastNightIndex >= 0
         */
        private void ensureEnoughNights(int lastNightIndex) {
            List<Night> nightList = patientAdmissionSchedule.getNightList();
            if (lastNightIndex >= nightList.size()) {
                long nightId = nightList.size();
                for (int j = nightList.size(); j <= lastNightIndex; j++) {
                    Night night = new Night();
                    night.setId(nightId);
                    night.setIndex(j);
                    nightList.add(night);
                    indexToNightMap.put(j, night);
                    nightId++;
                }
            }
        }


        // ************************************************************************
        // Helper methods
        // ************************************************************************

        private String[] splitBySpace(String line) {
            String[] lineTokens = line.split("\\ ");
            return lineTokens;
        }

        private String[] splitBySpace(String line, int numberOfTokens) {
            String[] lineTokens = line.split("\\ ");
            if (lineTokens.length != numberOfTokens) {
                throw new IllegalArgumentException("Read line (" + line
                        + ") is expected to contain " +  numberOfTokens + " tokens seperated by a space ( ).");
            }
            return lineTokens;
        }

        private String[] splitByPipeline(String line, int numberOfTokens) {
            String[] lineTokens = line.split("\\|");
            if (lineTokens.length != numberOfTokens) {
                throw new IllegalArgumentException("Read line (" + line
                        + ") is expected to contain " +  numberOfTokens + " tokens seperated by a pipeline (|).");
            }
            for (int i = 0; i < lineTokens.length; i++) {
                lineTokens[i] = lineTokens[i].trim();
            }
            return lineTokens;
        }

    }

}