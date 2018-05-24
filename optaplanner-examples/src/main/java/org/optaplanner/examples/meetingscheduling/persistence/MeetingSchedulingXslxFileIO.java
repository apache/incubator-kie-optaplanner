//TODO: need endingMinuteOfDay in class TimeGrain
//TODO: Meetings view, get duration from users in Minutes >> convert it to timeGrains

package org.optaplanner.examples.meetingscheduling.persistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaplanner.examples.meetingscheduling.app.MeetingSchedulingApp;
import org.optaplanner.examples.meetingscheduling.domain.Attendance;
import org.optaplanner.examples.meetingscheduling.domain.Day;
import org.optaplanner.examples.meetingscheduling.domain.Meeting;
import org.optaplanner.examples.meetingscheduling.domain.MeetingSchedule;
import org.optaplanner.examples.meetingscheduling.domain.Person;
import org.optaplanner.examples.meetingscheduling.domain.Room;
import org.optaplanner.examples.meetingscheduling.domain.TimeGrain;
import org.optaplanner.examples.nqueens.domain.Row;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;
import org.optaplanner.swing.impl.TangoColorFactory;

public class MeetingSchedulingXslxFileIO extends XStreamSolutionFileIO<MeetingSchedule>
    implements SolutionFileIO<MeetingSchedule> {

    protected static final DateTimeFormatter DAY_FORMATTER
        = DateTimeFormatter.ofPattern("E yyyy-MM-dd", Locale.ENGLISH);
    protected static final DateTimeFormatter TIME_FORMATTER
        = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    protected static final XSSFColor VIEW_TAB_COLOR = new XSSFColor(TangoColorFactory.BUTTER_1);

    protected static final XSSFColor UNAVAILABLE_COLOR = new XSSFColor(TangoColorFactory.ALUMINIUM_5);
    protected static final XSSFColor HARD_PENALTY_COLOR = new XSSFColor(TangoColorFactory.SCARLET_1);
    protected static final XSSFColor SOFT_PENALTY_COLOR = new XSSFColor(TangoColorFactory.ORANGE_1);

    // Meeting Parametrization:
    protected static final String DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE = "Do all meetings as soon as possible";

    protected static final String REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT = "Do all meetings as soon as possible";
    protected static final String PREFERRED_ATTENDANCE_CONFLICT = "Preferred attendance conflict";

    protected static final String ROOM_CONFLICT = "Room conflict";
    protected static final String DONT_GO_IN_OVERTIME = "Don't go in overtime";
    protected static final String REQUIRED_ATTENDANCE_CONFLICT = "Required attendance conflict";
    protected static final String REQUIRED_ROOM_CAPACITY = "Required room capacity";

//    @Override
//    public String getInputFileExtension() {
//        return "xlsx";
//    }

    @Override
    public String getOutputFileExtension() {
        return "xlsx";
    }

//    @Override
//    public MeetingSchedule read(File inputScheduleFile) {
//        try (InputStream in = new BufferedInputStream(new FileInputStream(inputScheduleFile))) {
//            XSSFWorkbook workbook = new XSSFWorkbook(in);
//            return new MeetingSchedulingXslxReader(workbook).read();
//        } catch (IOException | RuntimeException e) {
//            throw new IllegalStateException("Failed reading inputScheduleFile ("
//                                                + inputScheduleFile + ").", e);
//        }
//    }
//
//    // TODO: why static?
//    private static class MeetingSchedulingXslxReader {
//
//        protected final XSSFWorkbook workbook; // TODO: why protected?
//
//        protected MeetingSchedule schedule;
//        private Map<String, Meeting> totalMeetingMap;
//
//        protected XSSFSheet currentSheet;
//        protected Iterator<Row> currentRowIterator;
//        protected XSSFRow currentRow;
//        protected int currentRowNumber;
//        protected int currentColumnNumber;
//
//        public MeetingSchedulingXslxReader(XSSFWorkbook workbook) {
//            this.workbook = workbook;
//        }
//
//        public MeetingSchedule read() {
//            schedule = new MeetingSchedule();
//            totalMeetingMap = new HashMap<>();
//            readConfiguration();
//            readPersons();
//            readMeetings();
//            readDay();
//            readRooms();
//
//            return schedule;
//
//        }
//
//        private void readPersons() {
//        }
//
//        private void readConfiguration() {
//        }
//
//        private void readMeetings() {
//        }
//
//        private void readDay() {
//        }
//
//        private void readRooms() {
//        }
//
//    }

    @Override
    public void write(MeetingSchedule schedule, File outputScheduleFile) {
        try (FileOutputStream out = new FileOutputStream(outputScheduleFile)) {
            Workbook workbook = new MeetingSchedulingXslxWriter(schedule).write();
            workbook.write(out);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Failed writing outputScheduleFile ("
                                                + outputScheduleFile + ") for schedule (" + schedule + ").", e);
        }
    }

    private class MeetingSchedulingXslxWriter {

        protected final MeetingSchedule schedule;
        protected final List<ConstraintMatchTotal> constraintMatchTotalList;
        protected final Map<Object, Indictment> indictmentMap;

        protected XSSFWorkbook workbook;
        protected CreationHelper creationHelper;

        protected XSSFCellStyle headerStyle;
        protected XSSFCellStyle defaultStyle;
        protected XSSFCellStyle unavailableStyle;
        protected XSSFCellStyle hardPenaltyStyle;
        protected XSSFCellStyle mediumPenaltyStyle;
        protected XSSFCellStyle softPenaltyStyle;
        protected XSSFCellStyle wrappedStyle;

        protected XSSFSheet currentSheet;
        protected Drawing currentDrawing;
        protected XSSFRow currentRow;
        protected int currentRowNumber;
        protected int currentColumnNumber;
        protected int headerCellCount;

        public MeetingSchedulingXslxWriter(MeetingSchedule schedule) {
            this.schedule = schedule;
            ScoreDirectorFactory<MeetingSchedule> scoreDirectorFactory
                = SolverFactory.<MeetingSchedule>createFromXmlResource(MeetingSchedulingApp.SOLVER_CONFIG)
                .buildSolver().getScoreDirectorFactory();
            try (ScoreDirector<MeetingSchedule> scoreDirector = scoreDirectorFactory.buildScoreDirector()) {
                scoreDirector.setWorkingSolution(schedule);
                scoreDirector.calculateScore();
                constraintMatchTotalList = new ArrayList<>(scoreDirector.getConstraintMatchTotals());
                constraintMatchTotalList.sort(Comparator.comparing(ConstraintMatchTotal::getScore));
                indictmentMap = scoreDirector.getIndictmentMap();
            }
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

        private void createStyles() {
            headerStyle = createStyle(null);
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            defaultStyle = createStyle(null);
            unavailableStyle = createStyle(UNAVAILABLE_COLOR);
            hardPenaltyStyle = createStyle(HARD_PENALTY_COLOR);
            softPenaltyStyle = createStyle(SOFT_PENALTY_COLOR);
            wrappedStyle = createStyle(null);
        }

        private XSSFCellStyle createStyle(XSSFColor color) {
            XSSFCellStyle style = workbook.createCellStyle();
            if (color != null) {
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setFillForegroundColor(color);
            }
            style.setWrapText(true);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            return style;
        }

        private void writeConfiguration() {
            nextSheet("Configuration", 1, 3, false);
            nextRow();
            nextHeaderCell("Meeting name");
            nextCell().setCellValue("stub"); // TODO: add a meetingName field to class MeetingSchedule?
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
            autoSizeColumnsWithHeader();
        }

        private void writeConstraintLine(String name, Supplier<Integer> supplier, String constraintDescription) {
            nextRow();
            nextHeaderCell(name);
            //TODO: do we need to add weight to the constraints?
            nextCell().setCellValue("n/a");
            nextHeaderCell(constraintDescription);
        }

        private void writePersons() {
            nextSheet("Persons", 1, 0, false);
            nextRow();
            nextHeaderCell("Full name");
            for (Person person : schedule.getPersonList()) {
                nextRow();
                nextCell().setCellValue(person.getFullName());
            }
            autoSizeColumnsWithHeader();
        }

        // TODO: ensure that preferred/required attendance are in persons view
        private void writeMeetings() {
            nextSheet("Meetings", 2, 1, false);
            nextRow();
            nextHeaderCell("Id");
            nextHeaderCell("Topic");
            nextHeaderCell("Duration");
            nextHeaderCell("Required attendance list");
            nextHeaderCell("Preferred attendance list");
            for (Meeting meeting : schedule.getMeetingList()) {
                nextRow();
                nextCell().setCellValue(meeting.getId());
                nextCell().setCellValue(meeting.getTopic());
                nextCell().setCellValue(meeting.getDurationString());
                nextCell().setCellValue(
                    String.join(", ", meeting.getRequiredAttendanceList().stream()
                        .map(requiredAttendance -> requiredAttendance.getPerson().getFullName())
                        .collect(Collectors.toList())));
                nextCell().setCellValue(
                    String.join(", ", meeting.getPreferredAttendanceList().stream()
                        .map(preferredAttendance -> preferredAttendance.getPerson().getFullName())
                        .collect(Collectors.toList())));
            }
            autoSizeColumnsWithHeader();
        }

        private void writeDays() {
            nextSheet("Days", 1, 1, false);
            nextRow();
            nextHeaderCell("Day");
            nextHeaderCell("Start");
            nextHeaderCell("End");
            // TODO: A better way to represent day in class Day (LocalDate instead of int?)
            for (Day dayOfYear : schedule.getDayList()) {
                nextRow();
                LocalDate date = LocalDate.ofYearDay(Year.now().getValue(), dayOfYear.getDayOfYear());
                LocalTime startTime = LocalTime.ofSecondOfDay(8 * 60 * 60); // 8am TODO: set startTime field to class Day
                LocalTime endTime = LocalTime.ofSecondOfDay(17 * 60 * 60); // 5pm

                nextCell().setCellValue(DAY_FORMATTER.format(date));
                nextCell().setCellValue(TIME_FORMATTER.format(startTime));
                nextCell().setCellValue(TIME_FORMATTER.format(endTime));
            }
            autoSizeColumnsWithHeader();
        }

        private void writeRooms() {
            nextSheet("Rooms", 1, 1, false);
            nextRow();
            nextHeaderCell("");
            nextHeaderCell("");
            writeTimeGrainDaysHeaders();
            nextRow();
            nextHeaderCell("Name");
            nextHeaderCell("Capacity");
            writeTimeGrainsHoursHeaders();
            for (Room room : schedule.getRoomList()) {
                nextRow();
                nextCell().setCellValue(room.getName());
                nextCell().setCellValue(room.getCapacity());
                for (TimeGrain timeGrain : schedule.getTimeGrainList()) {
                    nextCell().setCellValue(""); // TODO: implement unavailable style after adding unavailableTimeGrain to class Room
                }
            }
            autoSizeColumnsWithHeader();
        }

        private void writeRoomsView() {
        }

        private void writePersonsView() {
        }

        private void writeTimeGrainDaysHeaders() {
            Day previousTimeGrainDay = null;
            int mergeStart = -1;

            for (TimeGrain timeGrain : schedule.getTimeGrainList()) {
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

        private void writeTimeGrainsHoursHeaders() {
            for (TimeGrain timeGrain: schedule.getTimeGrainList()) {
                LocalTime startTime = LocalTime.ofSecondOfDay(timeGrain.getStartingMinuteOfDay() * 60);
                LocalTime endTime = LocalTime.ofSecondOfDay(
                    (timeGrain.getStartingMinuteOfDay() + timeGrain.GRAIN_LENGTH_IN_MINUTES) * 60);
                nextHeaderCell(TIME_FORMATTER.format(startTime) + "-" + TIME_FORMATTER.format(endTime));
            }
        }

        protected void nextSheet(String sheetName, int colSplit, int rowSplit, boolean view) {
            currentSheet = workbook.createSheet(sheetName);
            currentDrawing = currentSheet.createDrawingPatriarch();
            currentSheet.createFreezePane(colSplit, rowSplit);
            currentRowNumber = -1;
            headerCellCount = 0;
            if (view) {
                currentSheet.setTabColor(VIEW_TAB_COLOR);
            }
        }

        protected void nextRow() {
            currentRowNumber++;
            currentRow = currentSheet.createRow(currentRowNumber);
            currentColumnNumber = -1;
        }

        protected void nextHeaderCell(String value) {
            nextCell(headerStyle).setCellValue(value);
            headerCellCount++;
        }

        protected XSSFCell nextCell() {
            return nextCell(defaultStyle);
        }

        protected XSSFCell nextCell(XSSFCellStyle cellStyle) {
            currentColumnNumber++;
            XSSFCell cell = currentRow.createCell(currentColumnNumber);
            cell.setCellStyle(cellStyle);
            return cell;
        }

        protected void autoSizeColumnsWithHeader() {
            for (int i = 0; i < headerCellCount; i++) {
                currentSheet.autoSizeColumn(i);
            }
        }
    }
}
