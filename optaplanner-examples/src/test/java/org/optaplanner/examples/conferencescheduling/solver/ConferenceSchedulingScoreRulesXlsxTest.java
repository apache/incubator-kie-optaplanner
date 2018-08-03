/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.conferencescheduling.solver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.optaplanner.core.api.domain.solution.cloner.SolutionCloner;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.examples.common.persistence.AbstractXlsxSolutionFileIO;
import org.optaplanner.examples.conferencescheduling.app.ConferenceSchedulingApp;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;
import org.optaplanner.examples.conferencescheduling.domain.Room;
import org.optaplanner.examples.conferencescheduling.domain.Talk;
import org.optaplanner.examples.conferencescheduling.domain.Timeslot;
import org.optaplanner.examples.conferencescheduling.persistence.ConferenceSchedulingXlsxFileIO;
import org.optaplanner.test.impl.score.buildin.hardsoft.HardSoftScoreVerifier;

import static org.optaplanner.examples.common.persistence.AbstractXlsxSolutionFileIO.DAY_FORMATTER;
import static org.optaplanner.examples.common.persistence.AbstractXlsxSolutionFileIO.TIME_FORMATTER;

@RunWith(Parameterized.class)
public class ConferenceSchedulingScoreRulesXlsxTest {

    private static final String testFileName = "testConferenceSchedulingScoreRules.xlsx";
    private static final HardSoftScore unassignedScore = HardSoftScore.ZERO;

    private String constraintPackage;
    private String constraintName;
    private HardSoftScore expectedScore;
    private ConferenceSolution solution;
    private String testSheetName;

    private static HardSoftScoreVerifier<ConferenceSolution> scoreVerifier = new HardSoftScoreVerifier<>(
            SolverFactory.createFromXmlResource(ConferenceSchedulingApp.SOLVER_CONFIG));

    public ConferenceSchedulingScoreRulesXlsxTest(String constraintPackage, String constraintName,
                                                  HardSoftScore expectedScore, ConferenceSolution solution, String testSheetName) {
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        this.expectedScore = expectedScore;
        this.solution = solution;
        this.testSheetName = testSheetName;
    }

    @Parameterized.Parameters(name = "{4}")
    public static Collection testSheetParameters() {
        List<Object[]> parametersList = new ArrayList<>();

        File testFile = new File(ConferenceSchedulingScoreRulesXlsxTest.class.getResource(testFileName).getFile());
        try (InputStream in = new BufferedInputStream(new FileInputStream(testFile))) {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            ConferenceSolution initialSolution = new ConferenceSchedulingXlsxFileIO(false).read(testFile);
            TestConferenceSchedulingScoreRulesReader testFileReader = new TestConferenceSchedulingScoreRulesReader(workbook, initialSolution);

            Object[] currentParameterList;
            while ((currentParameterList = testFileReader.nextTestSheetParameterList()) != null) {
                parametersList.add(currentParameterList);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Failed reading inputSolutionFile ("
                    + testFile.getName() + ").", e);
        }

        return parametersList;
    }

    @Test
    public void scoreRules() {
        scoreVerifier.assertHardWeight(constraintPackage, constraintName, expectedScore.getHardScore(), solution);
        scoreVerifier.assertSoftWeight(constraintPackage, constraintName, expectedScore.getSoftScore(), solution);
    }

    private static class TestConferenceSchedulingScoreRulesReader extends AbstractXlsxSolutionFileIO.AbstractXlsxReader<ConferenceSolution> {

        private final SolutionCloner<ConferenceSolution> solutionCloner =
                SolutionDescriptor.buildSolutionDescriptor(ConferenceSolution.class, Talk.class).getSolutionCloner();
        private final ConferenceSolution initialSolution;

        private int numberOfSheets, currentTestSheetIndex;
        private Map<String, Room> roomMap;
        private Map<Pair<LocalDateTime, LocalDateTime>, Timeslot> timeslotMap;
        private Map<Integer, LocalDate> columnIndexToDateMap;
        private Map<Integer, LocalTime> columnIndexToStartTimeMap;
        private Map<Integer, LocalTime> columnIndexToEndTimeMap;

        private TestConferenceSchedulingScoreRulesReader(XSSFWorkbook workbook, ConferenceSolution initialSolution) {
            super(workbook);
            this.numberOfSheets = workbook.getNumberOfSheets();
            this.currentTestSheetIndex = workbook.getSheetIndex("Talks") + 1;
            this.initialSolution = initialSolution;
            this.roomMap = initialSolution.getRoomList().stream().collect(
                    Collectors.toMap(Room::getName, Function.identity()));
            this.timeslotMap = initialSolution.getTimeslotList().stream().collect(
                    Collectors.toMap(timeslot -> Pair.of(timeslot.getStartDateTime(), timeslot.getEndDateTime()),
                            Function.identity()));
            this.columnIndexToDateMap = new HashMap<>(timeslotMap.size());
            this.columnIndexToStartTimeMap = new HashMap<>(timeslotMap.size());
            this.columnIndexToEndTimeMap = new HashMap<>(timeslotMap.size());
        }

        @Override
        public ConferenceSolution read() {
            return initialSolution;
        }

        private Object[] nextTestSheetParameterList() {
            String constraintPackage;
            String constraintName;
            HardSoftScore expectedScore;
            ConferenceSolution nextSheetSolution;
            String testSheetName;

            if (currentTestSheetIndex >= numberOfSheets) {
                return null;
            }

            nextSheet(workbook.getSheetName(currentTestSheetIndex++));
            testSheetName = currentSheet.getSheetName();

            nextRow(false);
            readHeaderCell("Constraint package");
            constraintPackage = nextStringCell().getStringCellValue();
            nextRow(false);
            readHeaderCell("Constraint name");
            constraintName = nextStringCell().getStringCellValue();
            nextRow(false);
            nextRow(false);
            readHeaderCell("Score");
            expectedScore = HardSoftScore.parseScore(nextStringCell().getStringCellValue());

            nextSheetSolution = solutionCloner.cloneSolution(initialSolution);
            Map<String, Talk> talkMap = nextSheetSolution.getTalkList().stream().collect(
                    Collectors.toMap(Talk::getCode, Function.identity()));

            scoreVerifier.assertHardWeight(constraintPackage, constraintName, unassignedScore.getHardScore(), nextSheetSolution);
            scoreVerifier.assertSoftWeight(constraintPackage, constraintName, unassignedScore.getSoftScore(), nextSheetSolution);

            nextRow();
            readTimeslotDays();
            nextRow(false);
            readHeaderCell("Room");
            readTimeslotHours();
            while (nextRow()) {
                String roomName = nextStringCell().getStringCellValue();
                Room room = roomMap.get(roomName);
                if (room == null) {
                    throw new IllegalStateException(currentPosition() + ": The room (" + roomName
                            + ") does not exist in the room list.");
                }
                for (int i = 0; i < columnIndexToStartTimeMap.size(); i++) {
                    String talkCode = nextCell().getStringCellValue();
                    if (!talkCode.isEmpty()) {
                        Talk talk = talkMap.get(talkCode);
                        if (talk == null) {
                            throw new IllegalStateException(currentPosition() + ": Talk (" + talkCode
                                    + ") does not exist in the talk list.");
                        }

                        LocalDateTime startDateTime = LocalDateTime.of(columnIndexToDateMap.get(currentColumnNumber),
                                columnIndexToStartTimeMap.get(currentColumnNumber));
                        LocalDateTime endDateTime = LocalDateTime.of(columnIndexToDateMap.get(currentColumnNumber),
                                columnIndexToEndTimeMap.get(currentColumnNumber));

                        Timeslot timeslot = timeslotMap.get(Pair.of(startDateTime, endDateTime));
                        if (timeslot == null) {
                            throw new IllegalStateException(currentPosition()
                                    + ": The timeslot with date (" + startDateTime.toLocalDate()
                                    + "), startTime (" + startDateTime.toLocalTime()
                                    + ") and endTime (" + endDateTime.toLocalTime()
                                    + ") doesn't exist in the other sheet (Timeslots).");
                        }
                        talk.setRoom(room);
                        talk.setTimeslot(timeslot);
                    }
                }
            }

            return new Object[]{constraintPackage, constraintName, expectedScore, nextSheetSolution, testSheetName};
        }

        private void readTimeslotDays() {
            columnIndexToDateMap.clear();
            String previousDateString = null;
            for (int i = 0; i < currentRow.getLastCellNum(); i++) {
                XSSFCell cell = currentRow.getCell(i);
                if (!cell.getStringCellValue().isEmpty() || previousDateString != null) {
                    if (!cell.getStringCellValue().isEmpty()) {
                        previousDateString = cell.getStringCellValue();
                    }
                    try {
                        columnIndexToDateMap.put(i, LocalDate.parse(previousDateString, DAY_FORMATTER));
                    } catch (DateTimeParseException e) {
                        throw new IllegalStateException(currentPosition() + ": The date (" + cell.getStringCellValue()
                                + ") does not parse as a date.");
                    }
                }
            }
        }

        private void readTimeslotHours() {
            columnIndexToStartTimeMap.clear();
            columnIndexToEndTimeMap.clear();
            StreamSupport.stream(currentRow.spliterator(), false)
                    .forEach(cell -> {
                        if (!cell.getStringCellValue().isEmpty() && !cell.getStringCellValue().equals("Room")) {
                            String[] startAndEndTimeStringArray = cell.getStringCellValue().split("-");
                            try {
                                columnIndexToStartTimeMap.put(cell.getColumnIndex(),
                                        LocalTime.parse(startAndEndTimeStringArray[0], TIME_FORMATTER));
                                columnIndexToEndTimeMap.put(cell.getColumnIndex(), LocalTime.parse(startAndEndTimeStringArray[1],
                                        TIME_FORMATTER));
                            } catch (DateTimeParseException e) {
                                throw new IllegalStateException(currentPosition() + ": The startTime (" + startAndEndTimeStringArray[0]
                                        + ") or endTime (" + startAndEndTimeStringArray[1]
                                        + ") doesn't parse as a time.", e);
                            }
                        }
                    });
        }
    }
}
