package org.optaplanner.examples.meetingscheduling.persistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.examples.common.persistence.AbstractXlsxSolutionFileIO;
import org.optaplanner.examples.meetingscheduling.app.MeetingSchedulingApp;
import org.optaplanner.examples.meetingscheduling.domain.Attendance;
import org.optaplanner.examples.meetingscheduling.domain.Day;
import org.optaplanner.examples.meetingscheduling.domain.Meeting;
import org.optaplanner.examples.meetingscheduling.domain.MeetingAssignment;
import org.optaplanner.examples.meetingscheduling.domain.MeetingSchedule;
import org.optaplanner.examples.meetingscheduling.domain.Person;
import org.optaplanner.examples.meetingscheduling.domain.PreferredAttendance;
import org.optaplanner.examples.meetingscheduling.domain.RequiredAttendance;
import org.optaplanner.examples.meetingscheduling.domain.Room;
import org.optaplanner.examples.meetingscheduling.domain.TimeGrain;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class MeetingSchedulingXlsxFileIO extends AbstractXlsxSolutionFileIO<MeetingSchedule> {

    // Meeting Parametrization:
    protected static final String DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE = "Do all meetings as soon as possible";

    protected static final String REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT = "Required and preferred attendance conflict";
    protected static final String PREFERRED_ATTENDANCE_CONFLICT = "Preferred attendance conflict";

    protected static final String ROOM_CONFLICT = "Room conflict";
    protected static final String DONT_GO_IN_OVERTIME = "Don't go in overtime";
    protected static final String REQUIRED_ATTENDANCE_CONFLICT = "Required attendance conflict";
    protected static final String REQUIRED_ROOM_CAPACITY = "Required room capacity";
    protected static final String START_AND_END_ON_SAME_DAY = "Start and end on same day";

    @Override
    public MeetingSchedule read(File inputScheduleFile) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputScheduleFile))) {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            return new MeetingSchedulingXslxReader(workbook).read();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Failed reading inputScheduleFile ("
                                                + inputScheduleFile + ").", e);
        }
    }

    private static class MeetingSchedulingXslxReader extends AbstractXslxReader<MeetingSchedule> {

        public MeetingSchedulingXslxReader(XSSFWorkbook workbook) {
            super(workbook);
        }

        public MeetingSchedule read() {
            solution = new MeetingSchedule();
            readConfiguration();
            readPersonList();
            readMeetingList();
            readDayList();
            readRoomList();

            return solution;
        }

        private void readConfiguration() {
            nextSheet("Configuration");
            nextRow();
            readHeaderCell("Meeting name");
            nextRow(true);
            readHeaderCell("Constraint");
            readHeaderCell("Weight");
            readHeaderCell("Description");

            readIntConstraintLine(DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE, null, "");
            readIntConstraintLine(REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT, null, "");
            readIntConstraintLine(PREFERRED_ATTENDANCE_CONFLICT, null, "");
            readIntConstraintLine(ROOM_CONFLICT, null, "");
            readIntConstraintLine(DONT_GO_IN_OVERTIME, null, "");
            readIntConstraintLine(REQUIRED_ATTENDANCE_CONFLICT, null, "");
            readIntConstraintLine(REQUIRED_ROOM_CAPACITY, null, "");
            readIntConstraintLine(START_AND_END_ON_SAME_DAY, null, "");
        }

        private void readPersonList() {
            nextSheet("Persons");
            nextRow(false);
            readHeaderCell("Full name");
            List<Person> personList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            long id = 0L;
            while (nextRow()) {
                Person person = new Person();
                person.setId(id++);
                person.setFullName(nextStringCell().getStringCellValue());
                if (!VALID_NAME_PATTERN.matcher(person.getFullName()).matches()) {
                    throw new IllegalStateException(
                        currentPosition() + ": The person name (" + person.getFullName()
                            + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
                }
                personList.add(person);
            }
            solution.setPersonList(personList);
        }

        private void readMeetingList() {
            Map<String, Person> personMap = solution.getPersonList().stream().collect(
                toMap(Person::getFullName, person -> person));
            nextSheet("Meetings");
            nextRow(false);
            readHeaderCell("Topic");
            readHeaderCell("Duration");
            readHeaderCell("Speakers");
            readHeaderCell("Content");
            readHeaderCell("Required attendance list");
            readHeaderCell("Preferred attendance list");

            List<Meeting> meetingList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            List<MeetingAssignment> meetingAssignmentList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            List<Attendance> attendanceList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            long meetingId = 0L, meetingAssignmentId = 0L, attendanceId = 0L;

            while (nextRow()) {
                Meeting meeting = new Meeting();
                MeetingAssignment meetingAssignment = new MeetingAssignment();
                meeting.setId(meetingId++);
                meetingAssignment.setId(meetingAssignmentId++);
                meeting.setTopic(nextStringCell().getStringCellValue());
                double durationDouble = nextNumericCell().getNumericCellValue();
                if (durationDouble <= 0 || durationDouble != Math.floor(durationDouble)) {
                    throw new IllegalStateException(
                        currentPosition() + ": The meeting with id (" + meeting.getId()
                            + ")'s has a duration (" + durationDouble + ") that isn't a strictly positive integer number.");
                }
                if (durationDouble % TimeGrain.GRAIN_LENGTH_IN_MINUTES != 0) {
                    throw new IllegalStateException(
                        currentPosition() + ": The meeting with id (" + meeting.getId()
                            + ") has a duration (" + durationDouble + ") that isn't a multiple of "
                            + TimeGrain.GRAIN_LENGTH_IN_MINUTES + ".");
                }
                meeting.setDurationInGrains((int) durationDouble / TimeGrain.GRAIN_LENGTH_IN_MINUTES);
                meeting.setSpeakerList(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                                           .filter(speaker -> !speaker.isEmpty())
                                           .map(speakerName -> {
                                               Person speaker = personMap.get(speakerName);
                                               if (speaker == null) {
                                                   throw new IllegalStateException(
                                                       currentPosition() + ": The meeting with id (" + meeting.getId()
                                                           + ") has a speaker (" + speakerName + ") that doesn't exist in the Persons list.");
                                               }
                                               return speaker;
                                           }).collect(toList()));
                meeting.setContent(nextStringCell().getStringCellValue());

                Set<Person> requiredPersonSet = new HashSet<>();
                List<RequiredAttendance> requiredAttendanceList = Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                    .filter(requiredAttendee -> !requiredAttendee.isEmpty())
                    .map(personName -> {
                        RequiredAttendance requiredAttendance = new RequiredAttendance();
                        Person person = personMap.get(personName);
                        if (person == null) {
                            throw new IllegalStateException(
                                currentPosition() + ": The meeting with id (" + meeting.getId()
                                    + ") has a required attendee (" + personName + ") that doesn't exist in the Persons list.");
                        }
                        if (requiredPersonSet.contains(person)) {
                            throw new IllegalStateException(
                                currentPosition() + ": The meeting with id (" + meeting.getId()
                                    + ") has a duplicate required attendee (" + personName + ").");
                        }
                        requiredPersonSet.add(person);
                        requiredAttendance.setMeeting(meeting);
                        requiredAttendance.setPerson(person);
                        return requiredAttendance;
                    })
                    .collect(toList());
                for (RequiredAttendance requiredAttendance : requiredAttendanceList) {
                    requiredAttendance.setId(attendanceId++);
                }
                meeting.setRequiredAttendanceList(requiredAttendanceList);
                attendanceList.addAll(requiredAttendanceList);

                Set<Person> preferredPersonSet = new HashSet<>();
                List<PreferredAttendance> preferredAttendanceList = Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                    .filter(preferredAttendee -> !preferredAttendee.isEmpty())
                    .map(personName -> {
                        PreferredAttendance preferredAttendance = new PreferredAttendance();
                        Person person = personMap.get(personName);
                        if (person == null) {
                            throw new IllegalStateException(
                                currentPosition() + ": The meeting with id (" + meeting.getId()
                                    + ") has a preferred attendee (" + personName + ") that doesn't exist in the Persons list.");
                        }
                        if (preferredPersonSet.contains(person)) {
                            throw new IllegalStateException(
                                currentPosition() + ": The meeting with id (" + meeting.getId()
                                    + ") has a duplicate preferred attendee (" + personName + ").");
                        }
                        if (requiredPersonSet.contains(person)) {
                            throw new IllegalStateException(
                                currentPosition() + ": The meeting with id (" + meeting.getId()
                                    + ") has a preferred attendee (" + personName + ") that is also a required attendee.");
                        }
                        preferredPersonSet.add(person);
                        preferredAttendance.setMeeting(meeting);
                        preferredAttendance.setPerson(person);
                        return preferredAttendance;
                    })
                    .collect(toList());
                for (PreferredAttendance preferredAttendance : preferredAttendanceList) {
                    preferredAttendance.setId(attendanceId++);
                }
                meeting.setPreferredAttendanceList(preferredAttendanceList);
                attendanceList.addAll(preferredAttendanceList);

                meetingList.add(meeting);
                meetingAssignment.setMeeting(meeting);
                meetingAssignmentList.add(meetingAssignment);
            }
            solution.setMeetingList(meetingList);
            solution.setMeetingAssignmentList(meetingAssignmentList);
            solution.setAttendanceList(attendanceList);
        }

        private void readDayList() {
            nextSheet("Days");
            nextRow(false);
            readHeaderCell("Day");
            readHeaderCell("Start");
            readHeaderCell("End");
            List<Day> dayList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            List<TimeGrain> timeGrainList = new ArrayList<>();
            long dayId = 0L, timeGrainId = 0L;
            while (nextRow()) {
                Day day = new Day();
                day.setId(dayId++);
                day.setDayOfYear(LocalDate.parse(nextStringCell().getStringCellValue(), DAY_FORMATTER).getDayOfYear());
                dayList.add(day);

                LocalTime startTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                LocalTime endTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                LocalTime lunchHourStartTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                int startMinuteOfDay = startTime.getHour() * 60 + startTime.getMinute();
                int endMinuteOfDay = endTime.getHour() * 60 + endTime.getMinute();
                int lunchHourStartMinuteOfDay = lunchHourStartTime.getHour() * 60 + lunchHourStartTime.getMinute();
                for (int i = 0; (endMinuteOfDay - startMinuteOfDay) > i * TimeGrain.GRAIN_LENGTH_IN_MINUTES; i++) {
                    int timeGrainStartingMinuteOfDay = i * TimeGrain.GRAIN_LENGTH_IN_MINUTES + startMinuteOfDay;
                    if (timeGrainStartingMinuteOfDay < lunchHourStartMinuteOfDay
                        || timeGrainStartingMinuteOfDay >= lunchHourStartMinuteOfDay + 60) {
                        TimeGrain timeGrain = new TimeGrain();
                        timeGrain.setId(timeGrainId);
                        timeGrain.setGrainIndex((int) timeGrainId++);
                        timeGrain.setDay(day);
                        timeGrain.setStartingMinuteOfDay(timeGrainStartingMinuteOfDay);
                        timeGrainList.add(timeGrain);
                    }
                }
            }
            solution.setDayList(dayList);
            solution.setTimeGrainList(timeGrainList);
        }

        private void readRoomList() {
            nextSheet("Rooms");
            nextRow();
            readHeaderCell("Name");
            readHeaderCell("Capacity");
            List<Room> roomList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            long id = 0L;
            while (nextRow()) {
                Room room = new Room();
                room.setId(id++);
                room.setName(nextStringCell().getStringCellValue());
                if (!VALID_NAME_PATTERN.matcher(room.getName()).matches()) {
                    throw new IllegalStateException(
                        currentPosition() + ": The room name (" + room.getName()
                            + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
                }
                double capacityDouble = nextNumericCell().getNumericCellValue();
                if (capacityDouble <= 0 || capacityDouble != Math.floor(capacityDouble)) {
                    throw new IllegalStateException(
                        currentPosition() + ": The room with name (" + room.getName()
                            + ") has a capacity (" + capacityDouble + ") that isn't a strictly positive integer number.");
                }
                room.setCapacity((int) capacityDouble);
                roomList.add(room);
            }
            solution.setRoomList(roomList);
        }
    }

    @Override
    public void write(MeetingSchedule solution, File outputScheduleFile) {
        try (FileOutputStream out = new FileOutputStream(outputScheduleFile)) {
            Workbook workbook = new MeetingSchedulingXlsxWriter(solution).write();
            workbook.write(out);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Failed writing outputScheduleFile (" + outputScheduleFile
                                                + ") for schedule (" + solution + ").", e);
        }
    }

    private class MeetingSchedulingXlsxWriter extends AbstractXlsxWriter<MeetingSchedule> {

        public MeetingSchedulingXlsxWriter(MeetingSchedule solution) {
            super(solution, MeetingSchedulingApp.SOLVER_CONFIG);
        }

        public Workbook write() {
            workbook = new XSSFWorkbook();
            creationHelper = workbook.getCreationHelper();
            createStyles();
            writeConfiguration();
            writePersons();
            writeMeetings();
            writeDays();
            writeRooms();
            writeRoomsView();
            writePersonsView();

            return workbook;
        }

        private void writeConfiguration() {
            nextSheet("Configuration", 1, 3, false);
            nextRow();
            nextHeaderCell("Meeting name");
            nextCell().setCellValue(""); // TODO: add a meetingName field to class MeetingSchedule?
            nextRow();
            nextRow();
            nextHeaderCell("Constraint");
            nextHeaderCell("Weight");
            nextHeaderCell("Description");

            writeConstraintLine(DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE, null, "");
            nextRow();
            writeConstraintLine(REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT, null, "");
            writeConstraintLine(PREFERRED_ATTENDANCE_CONFLICT, null, "");
            nextRow();
            writeConstraintLine(ROOM_CONFLICT, null, "");
            writeConstraintLine(DONT_GO_IN_OVERTIME, null, "");
            writeConstraintLine(REQUIRED_ATTENDANCE_CONFLICT, null, "");
            writeConstraintLine(REQUIRED_ROOM_CAPACITY, null, "");
            writeConstraintLine(START_AND_END_ON_SAME_DAY, null, "");
            autoSizeColumnsWithHeader();
        }

        private void writeConstraintLine(String name, Supplier<Integer> supplier, String constraintDescription) {
            nextRow();
            nextHeaderCell(name);
            nextCell().setCellValue("n/a");
            nextHeaderCell(constraintDescription);
        }

        private void writePersons() {
            nextSheet("Persons", 1, 0, false);
            nextRow();
            nextHeaderCell("Full name");
            for (Person person : solution.getPersonList()) {
                nextRow();
                nextCell().setCellValue(person.getFullName());
            }
            autoSizeColumnsWithHeader();
        }

        private void writeMeetings() {
            nextSheet("Meetings", 2, 1, false);
            nextRow();
            nextHeaderCell("Topic");
            nextHeaderCell("Duration");
            nextHeaderCell("Speakers");
            nextHeaderCell("Content");
            nextHeaderCell("Required attendance list");
            nextHeaderCell("Preferred attendance list");
            for (Meeting meeting : solution.getMeetingList()) {
                nextRow();
                nextCell().setCellValue(meeting.getTopic());
                nextCell().setCellValue(meeting.getDurationInGrains() * TimeGrain.GRAIN_LENGTH_IN_MINUTES);
                nextCell().setCellValue(meeting.getSpeakerList() == null ? "" :
                                            String.join(", ", meeting.getSpeakerList().stream()
                                                .map(speaker -> speaker.getFullName())
                                                .collect(toList())));
                nextCell().setCellValue(meeting.getContent() == null ? "" : meeting.getContent());
                nextCell().setCellValue(
                    String.join(", ", meeting.getRequiredAttendanceList().stream()
                        .map(requiredAttendance -> requiredAttendance.getPerson().getFullName())
                        .collect(toList())));
                nextCell().setCellValue(
                    String.join(", ", meeting.getPreferredAttendanceList().stream()
                        .map(preferredAttendance -> preferredAttendance.getPerson().getFullName())
                        .collect(toList())));
            }
            autoSizeColumnsWithHeader();
        }

        private void writeDays() {
            nextSheet("Days", 1, 1, false);
            nextRow();
            nextHeaderCell("Day");
            nextHeaderCell("Start");
            nextHeaderCell("End");
            nextHeaderCell("Lunch hour start time");
            for (Day dayOfYear : solution.getDayList()) {
                nextRow();
                LocalDate date = LocalDate.ofYearDay(Year.now().getValue(), dayOfYear.getDayOfYear());
                int startMinuteOfDay = 24 * 60, endMinuteOfDay = 0;
                for (TimeGrain timeGrain : solution.getTimeGrainList()) {
                    if (timeGrain.getDay().equals(dayOfYear)) {
                        startMinuteOfDay = timeGrain.getStartingMinuteOfDay() < startMinuteOfDay ?
                            timeGrain.getStartingMinuteOfDay() : startMinuteOfDay;
                        endMinuteOfDay = timeGrain.getStartingMinuteOfDay() + TimeGrain.GRAIN_LENGTH_IN_MINUTES > endMinuteOfDay ?
                            timeGrain.getStartingMinuteOfDay() + TimeGrain.GRAIN_LENGTH_IN_MINUTES : endMinuteOfDay;
                    }
                }
                LocalTime startTime = LocalTime.ofSecondOfDay(startMinuteOfDay * 60); // 8am TODO: set start/end Time fields to class Day
                LocalTime endTime = LocalTime.ofSecondOfDay(endMinuteOfDay * 60); // 6pm
                LocalTime lunchHourStartTime = LocalTime.ofSecondOfDay(12 * 60 * 60); // 12 pm

                nextCell().setCellValue(DAY_FORMATTER.format(date));
                nextCell().setCellValue(TIME_FORMATTER.format(startTime));
                nextCell().setCellValue(TIME_FORMATTER.format(endTime));
                nextCell().setCellValue(TIME_FORMATTER.format(lunchHourStartTime));
            }
            autoSizeColumnsWithHeader();
        }

        private void writeRooms() {
            nextSheet("Rooms", 1, 1, false);
            nextRow();
            nextHeaderCell("Name");
            nextHeaderCell("Capacity");
            for (Room room : solution.getRoomList()) {
                nextRow();
                nextCell().setCellValue(room.getName());
                nextCell().setCellValue(room.getCapacity());
            }
            autoSizeColumnsWithHeader();
        }

        private void writeRoomsView() {
            nextSheet("Rooms view", 1, 2, true);
            String[] filteredConstraintNames = {
                DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,

                REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT, PREFERRED_ATTENDANCE_CONFLICT,

                ROOM_CONFLICT, DONT_GO_IN_OVERTIME, REQUIRED_ATTENDANCE_CONFLICT, REQUIRED_ROOM_CAPACITY};
            nextRow();
            nextHeaderCell("");
            writeTimeGrainDaysHeaders();
            nextRow();
            nextHeaderCell("Room");
            writeTimeGrainHoursHeaders();
            for (Room room : solution.getRoomList()) {
                nextRow();
                currentRow.setHeightInPoints(2 * currentSheet.getDefaultRowHeightInPoints());
                nextCell().setCellValue(room.getName());
                List<MeetingAssignment> roomMeetingAssignmentList = solution.getMeetingAssignmentList().stream()
                    .filter(meetingAssignment -> meetingAssignment.getRoom() == room).collect(toList());

                List<MeetingAssignment> mergePreviousMeetingList = null;
                int mergeStart = -1;
                int previousMeetingRemainingTimeGrains = 0;
                for (TimeGrain timeGrain : solution.getTimeGrainList()) {
                    List<MeetingAssignment> meetingAssignmentList = roomMeetingAssignmentList.stream()
                        .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() == timeGrain)
                        .collect(toList());
                    if (meetingAssignmentList.isEmpty() && mergePreviousMeetingList != null && previousMeetingRemainingTimeGrains > 0) {
                        previousMeetingRemainingTimeGrains--;
                        nextCell();
                    } else {
                        if (mergePreviousMeetingList != null && mergeStart < currentColumnNumber) {
                            currentSheet.addMergedRegion(new CellRangeAddress(currentRowNumber, currentRowNumber, mergeStart, currentColumnNumber));
                        }
                        nextMeetingAssignmentListCell(meetingAssignmentList,
                                                      meetingAssignment -> meetingAssignment.getMeeting().getTopic() + "\n  "
                                                          + meetingAssignment.getMeeting().getSpeakerList().
                                                          stream().map(Person::getFullName).collect(joining(", ")),
                                                      filteredConstraintNames);
                        mergePreviousMeetingList = meetingAssignmentList.isEmpty() ? null : meetingAssignmentList;
                        mergeStart = currentColumnNumber;
                        int longestDurationInGrains = 1;
                        for (MeetingAssignment meetingAssignment : meetingAssignmentList) {
                            if (meetingAssignment.getMeeting().getDurationInGrains() > longestDurationInGrains) {
                                longestDurationInGrains = meetingAssignment.getMeeting().getDurationInGrains();
                            }
                        }
                        previousMeetingRemainingTimeGrains = longestDurationInGrains - 1;
                    }
                }
                if (mergePreviousMeetingList != null && mergeStart < currentColumnNumber) {
                    currentSheet.addMergedRegion(new CellRangeAddress(currentRowNumber, currentRowNumber, mergeStart, currentColumnNumber));
                }
            }
            autoSizeColumnsWithHeader();
        }

        private void writePersonsView() {
            nextSheet("Persons view", 2, 2, true);
            nextRow();
            nextHeaderCell("");
            nextHeaderCell("");
            writeTimeGrainDaysHeaders();
            nextRow();
            nextHeaderCell("Person");
            nextHeaderCell("Attendance");
            writeTimeGrainHoursHeaders();
            for (Person person : solution.getPersonList()) {
                writePersonMeetingList(person, true);
                writePersonMeetingList(person, false);
            }
            autoSizeColumnsWithHeader();
        }

        private void writePersonMeetingList(Person person, boolean required) {
            String[] filteredConstraintNames = {
                DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,

                REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT, PREFERRED_ATTENDANCE_CONFLICT,

                ROOM_CONFLICT, DONT_GO_IN_OVERTIME, REQUIRED_ATTENDANCE_CONFLICT, REQUIRED_ROOM_CAPACITY};
            nextRow();
            currentRow.setHeightInPoints(2 * currentSheet.getDefaultRowHeightInPoints());
            nextHeaderCell(person.getFullName());
            if (required) {
                nextHeaderCell("Required");
            } else {
                currentSheet.addMergedRegion(new CellRangeAddress(currentRowNumber - 1, currentRowNumber, currentColumnNumber, currentColumnNumber));
                nextHeaderCell("Preferred");
            }

            List<Meeting> personMeetingList;
            if (required) {
                personMeetingList = solution.getAttendanceList().stream()
                    .filter(attendance -> attendance.getPerson().equals(person)
                        && attendance.getMeeting().getRequiredAttendanceList().contains(attendance))
                    .map(Attendance::getMeeting)
                    .collect(toList());
            } else {
                personMeetingList = solution.getAttendanceList().stream()
                    .filter(attendance -> attendance.getPerson().equals(person)
                        && attendance.getMeeting().getPreferredAttendanceList().contains(attendance))
                    .map(Attendance::getMeeting)
                    .collect(toList());
            }
            List<MeetingAssignment> timeGrainMeetingAssignmentList = solution.getMeetingAssignmentList().stream()
                .filter(meetingAssignment -> personMeetingList.contains(meetingAssignment.getMeeting()))
                .collect(toList());

            TimeGrain mergePreviousTimeGrain = null;
            int columnMergeStart = -1;
            int previousMeetingRemainingTimeGrains = 0;
            for (TimeGrain timeGrain : solution.getTimeGrainList()) {
                List<MeetingAssignment> meetingAssignmentList = timeGrainMeetingAssignmentList.stream()
                    .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null
                        && meetingAssignment.getStartingTimeGrain().equals(timeGrain))
                    .collect(toList());
                if (meetingAssignmentList.isEmpty() && mergePreviousTimeGrain != null && previousMeetingRemainingTimeGrains > 0) {
                    previousMeetingRemainingTimeGrains--;
                    nextCell();
                } else {
                    if (mergePreviousTimeGrain != null && columnMergeStart < currentColumnNumber) {
                        currentSheet.addMergedRegion(new CellRangeAddress(currentRowNumber, currentRowNumber, columnMergeStart, currentColumnNumber));
                    }
                    nextMeetingAssignmentListCell(meetingAssignmentList,
                                                  meetingAssignment -> meetingAssignment.getMeeting().getTopic() + "\n  "
                                                      + meetingAssignment.getMeeting().getSpeakerList()
                                                      .stream().map(Person::getFullName).collect(joining(", ")),
                                                  filteredConstraintNames);
                    mergePreviousTimeGrain = meetingAssignmentList.isEmpty() ? null : timeGrain;
                    columnMergeStart = currentColumnNumber;
                    int longestDurationInGrains = 1;
                    for (MeetingAssignment meetingAssignment : meetingAssignmentList) {
                        if (meetingAssignment.getMeeting().getDurationInGrains() > longestDurationInGrains) {
                            longestDurationInGrains = meetingAssignment.getMeeting().getDurationInGrains();
                        }
                    }
                    previousMeetingRemainingTimeGrains = longestDurationInGrains - 1;
                }
            }
        }

        private void writeTimeGrainDaysHeaders() {
            Day previousTimeGrainDay = null;
            int mergeStart = -1;

            for (TimeGrain timeGrain : solution.getTimeGrainList()) {
                Day timeGrainDay = timeGrain.getDay();
                if (timeGrainDay.equals(previousTimeGrainDay)) {
                    nextHeaderCell("");
                } else {
                    if (previousTimeGrainDay != null) {
                        currentSheet.addMergedRegion(new CellRangeAddress(currentRowNumber, currentRowNumber, mergeStart, currentColumnNumber));
                    }
                    nextHeaderCell(DAY_FORMATTER.format(
                        LocalDate.ofYearDay(Year.now().getValue(), timeGrainDay.getDayOfYear())));
                    previousTimeGrainDay = timeGrainDay;
                    mergeStart = currentColumnNumber;
                }
            }
            if (previousTimeGrainDay != null) {
                currentSheet.addMergedRegion(new CellRangeAddress(currentRowNumber, currentRowNumber, mergeStart, currentColumnNumber));
            }
        }

        private void writeTimeGrainHoursHeaders() {
            for (TimeGrain timeGrain : solution.getTimeGrainList()) {
                LocalTime startTime = LocalTime.ofSecondOfDay(timeGrain.getStartingMinuteOfDay() * 60);
                LocalTime endTime = LocalTime.ofSecondOfDay(
                    (timeGrain.getStartingMinuteOfDay() + timeGrain.GRAIN_LENGTH_IN_MINUTES) * 60);
                nextHeaderCell(TIME_FORMATTER.format(startTime));
            }
        }

        protected void nextMeetingAssignmentListCell(List<MeetingAssignment> meetingAssignmentList, Function<MeetingAssignment, String> stringFunction,
                                                     String[] filteredConstraintNames) {
            nextMeetingAssignmentListCell(false, meetingAssignmentList, stringFunction, filteredConstraintNames);
        }

        protected void nextMeetingAssignmentListCell(boolean unavailable, List<MeetingAssignment> meetingAssignmentList,
                                                     Function<MeetingAssignment, String> stringFunction, String[] filteredConstraintNames) {
            List<String> filteredConstraintNameList = (filteredConstraintNames == null) ? null
                : Arrays.asList(filteredConstraintNames);
            if (meetingAssignmentList == null) {
                meetingAssignmentList = Collections.emptyList();
            }
            HardMediumSoftScore score = meetingAssignmentList.stream()
                .map(indictmentMap::get).filter(Objects::nonNull)
                .flatMap(indictment -> indictment.getConstraintMatchSet().stream())
                // Filter out filtered constraints
                .filter(constraintMatch -> filteredConstraintNameList == null
                    || filteredConstraintNameList.contains(constraintMatch.getConstraintName()))
                .map(constraintMatch -> (HardMediumSoftScore) constraintMatch.getScore())
                // Filter out positive constraints
                .filter(indictmentScore -> !(indictmentScore.getHardScore() >= 0 && indictmentScore.getSoftScore() >= 0))
                .reduce(Score::add).orElse(HardMediumSoftScore.ZERO);
            XSSFCell cell;
            if (!score.isFeasible()) {
                cell = nextCell(hardPenaltyStyle);
            } else if (unavailable) {
                cell = nextCell(unavailableStyle);
            } else if (score.getSoftScore() < 0) {
                cell = nextCell(softPenaltyStyle);
            } else {
                cell = nextCell(wrappedStyle);
            }

            if (!meetingAssignmentList.isEmpty()) {
                ClientAnchor anchor = creationHelper.createClientAnchor();
                anchor.setCol1(cell.getColumnIndex());
                anchor.setCol2(cell.getColumnIndex() + 4);
                anchor.setRow1(currentRow.getRowNum());
                anchor.setRow2(currentRow.getRowNum() + 4);
                Comment comment = currentDrawing.createCellComment(anchor);
                StringBuilder commentString = new StringBuilder(meetingAssignmentList.size() * 200);
                for (MeetingAssignment meetingAssignment : meetingAssignmentList) {
                    commentString.append("Topic: " + meetingAssignment.getMeeting().getTopic()).append("\n")
                        .append("Speakers: " + meetingAssignment.getMeeting().getSpeakerList().stream().map(Person::getFullName).collect(joining(", ")) + "\n")
                        .append("Date and Time: " + meetingAssignment.getStartingTimeGrain().getDateTimeString() + "\n")
                        .append("Duration: " + meetingAssignment.getMeeting().getDurationInGrains() * TimeGrain.GRAIN_LENGTH_IN_MINUTES + " minutes.\n")
                        .append("Room: " + meetingAssignment.getRoom().getName() + "\n");

                    Indictment indictment = indictmentMap.get(meetingAssignment);
                    if (indictment != null) {
                        commentString.append("\n").append(indictment.getScore().toShortString())
                            .append(" total");
                        Set<ConstraintMatch> constraintMatchSet = indictment.getConstraintMatchSet();
                        List<String> constraintNameList = constraintMatchSet.stream()
                            .map(ConstraintMatch::getConstraintName).distinct().collect(toList());
                        for (String constraintName : constraintNameList) {
                            List<ConstraintMatch> filteredConstraintMatchList = constraintMatchSet.stream()
                                .filter(constraintMatch -> constraintMatch.getConstraintName().equals(constraintName))
                                .collect(toList());
                            Score sum = filteredConstraintMatchList.stream()
                                .map(ConstraintMatch::getScore)
                                .reduce(Score::add).orElse(HardSoftScore.ZERO);
                            String justificationTalkCodes = filteredConstraintMatchList.stream()
                                .flatMap(constraintMatch -> constraintMatch.getJustificationList().stream())
                                .filter(justification -> justification instanceof MeetingAssignment && justification != meetingAssignment)
                                .distinct().map(o -> Long.toString(((MeetingAssignment) o).getMeeting().getId())).collect(joining(", "));
                            commentString.append("\n    ").append(sum.toShortString())
                                .append(" for ").append(filteredConstraintMatchList.size())
                                .append(" ").append(constraintName).append("s")
                                .append("\n        ").append(justificationTalkCodes);
                        }
                    }
                    commentString.append("\n\n");
                }
                comment.setString(creationHelper.createRichTextString(commentString.toString()));
                cell.setCellComment(comment);
            }
            cell.setCellValue(meetingAssignmentList.stream().map(stringFunction).collect(joining("\n")));
            currentRow.setHeightInPoints(Math.max(currentRow.getHeightInPoints(), meetingAssignmentList.size() * currentSheet.getDefaultRowHeightInPoints()));
        }
    }
}
