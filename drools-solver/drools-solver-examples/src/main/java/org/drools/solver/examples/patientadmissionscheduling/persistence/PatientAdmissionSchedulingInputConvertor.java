package org.drools.solver.examples.patientadmissionscheduling.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.drools.solver.examples.common.persistence.AbstractInputConvertor;
import org.drools.solver.examples.patientadmissionscheduling.domain.PatientAdmissionSchedule;
import org.drools.solver.examples.patientadmissionscheduling.domain.Specialism;
import org.drools.solver.examples.patientadmissionscheduling.domain.Department;
import org.drools.solver.examples.patientadmissionscheduling.domain.DepartmentSpecialism;
import org.drools.solver.examples.patientadmissionscheduling.domain.Equipment;
import org.drools.solver.examples.patientadmissionscheduling.domain.Room;
import org.drools.solver.examples.patientadmissionscheduling.domain.RoomSpecialism;
import org.drools.solver.examples.patientadmissionscheduling.domain.GenderLimitation;
import org.drools.solver.examples.patientadmissionscheduling.domain.RoomEquipment;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionSchedulingInputConvertor extends AbstractInputConvertor {

    public static void main(String[] args) {
        new PatientAdmissionSchedulingInputConvertor().convertAll();
    }

    protected String getExampleDirName() {
        return "patientadmissionscheduling";
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
        private int patientListSize;
        private int nightListSize;

        private Map<Long, Specialism> idToSpecialismMap = null;
        private Map<Long, Department> idToDepartmentMap = null;
        private Map<Integer, Equipment> indexToEquipmentMap = null;
        private Map<Long, Room> idToRoomMap = null;

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
                            + ") after pipeline (|) seperated by a space ( ).");
                }
                for (int j = 0; j < departmentSpecialismTokens.length; j += 2) {
                    DepartmentSpecialism departmentSpecialism = new DepartmentSpecialism();
                    departmentSpecialism.setId((long) j / 2);
                    departmentSpecialism.setDepartment(department);
                    departmentSpecialism.setPriority(Integer.parseInt(departmentSpecialismTokens[j]));
                    departmentSpecialism.setSpecialism(idToSpecialismMap.get(
                            Long.parseLong(departmentSpecialismTokens[j + 1])));
                    departmentSpecialismList.add(departmentSpecialism);
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
                            + ") after pipeline (|) seperated by a space ( ).");
                }
                for (int j = 0; j < roomSpecialismTokens.length; j += 2) {
                    int priority = Integer.parseInt(roomSpecialismTokens[j]);
                    long specialismId = Long.parseLong(roomSpecialismTokens[j + 1]);
                    RoomSpecialism roomSpecialism = new RoomSpecialism();
                    roomSpecialism.setId((long) j / 2);
                    roomSpecialism.setRoom(room);
                    roomSpecialism.setSpecialism(idToSpecialismMap.get(specialismId));
                    roomSpecialism.setPriority(priority);
                    roomSpecialismList.add(roomSpecialism);
                }

                String[] roomEquipmentTokens = splitBySpace(lineTokens[5]);
                if (roomEquipmentTokens.length % 2 != 0) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain even number of tokens (" + roomEquipmentTokens.length
                            + ") after pipeline (|) seperated by a space ( ).");
                }
                for (int j = 0; j < roomEquipmentTokens.length; j++) {
                    int hasEquipment = Integer.parseInt(roomEquipmentTokens[j]);
                    if (hasEquipment == 1) {
                        RoomEquipment roomEquipment = new RoomEquipment();
                        roomEquipment.setId((long) j);
                        roomEquipment.setRoom(room);
                        roomEquipment.setEquipment(indexToEquipmentMap.get(j));
                        roomEquipmentList.add(roomEquipment);
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