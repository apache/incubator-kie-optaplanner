//TODO: need endingMinuteOfDay in class TimeGrain

package org.optaplanner.examples.meetingscheduling.persistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.optaplanner.examples.meetingscheduling.domain.Meeting;
import org.optaplanner.examples.meetingscheduling.domain.MeetingSchedule;
import org.optaplanner.examples.nqueens.domain.Row;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;
import org.optaplanner.swing.impl.TangoColorFactory;

public class MeetingSchedulingXslxFileIO extends XStreamSolutionFileIO<MeetingSchedule>
    implements SolutionFileIO<MeetingSchedule> {

    protected static final XSSFColor VIEW_TAB_COLOR = new XSSFColor(TangoColorFactory.BUTTER_1);

    protected static final XSSFColor UNAVAILABLE_COLOR = new XSSFColor(TangoColorFactory.ALUMINIUM_5);
    protected static final XSSFColor HARD_PENALTY_COLOR = new XSSFColor(TangoColorFactory.SCARLET_1);
    protected static final XSSFColor SOFT_PENALTY_COLOR = new XSSFColor(TangoColorFactory.ORANGE_1);

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
        }

        private void writePersons() {
        }

        private void writeMeetings() {
        }

        private void writeDays() {
        }

        private void writeRooms() {
        }

        private void writeRoomsView() {
        }

        private void writePersonsView() {
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
    }
}
