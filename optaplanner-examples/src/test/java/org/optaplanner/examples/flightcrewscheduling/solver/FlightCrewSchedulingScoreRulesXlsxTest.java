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

package org.optaplanner.examples.flightcrewscheduling.solver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.optaplanner.core.api.domain.solution.cloner.SolutionCloner;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.examples.common.persistence.AbstractXlsxSolutionFileIO;
import org.optaplanner.examples.flightcrewscheduling.app.FlightCrewSchedulingApp;
import org.optaplanner.examples.flightcrewscheduling.domain.Employee;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightAssignment;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightCrewSolution;
import org.optaplanner.examples.flightcrewscheduling.persistence.FlightCrewSchedulingXlsxFileIO;
import org.optaplanner.test.impl.score.buildin.hardsoftlong.HardSoftLongScoreVerifier;

@RunWith(Parameterized.class)
public class FlightCrewSchedulingScoreRulesXlsxTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    private static final String testFileName = "testFlightCrewSchedulingScoreRules.xlsx";
    private static final HardSoftLongScore unassignedScore = HardSoftLongScore.ZERO;

    private String constraintPackage;
    private String constraintName;
    private HardSoftLongScore expectedScore;
    private FlightCrewSolution solution;
    private String testSheetName;

    private static HardSoftLongScoreVerifier<FlightCrewSolution> scoreVerifier = new HardSoftLongScoreVerifier<>(
            SolverFactory.createFromXmlResource(FlightCrewSchedulingApp.SOLVER_CONFIG));

    public FlightCrewSchedulingScoreRulesXlsxTest(String constraintPackage, String constraintName,
            HardSoftLongScore expectedScore, FlightCrewSolution solution, String testSheetName) {
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        this.expectedScore = expectedScore;
        this.solution = solution;
        this.testSheetName = testSheetName;
    }

    @Parameterized.Parameters(name = "{4}")
    public static Collection<Object[]> testSheetParameters() {
        List<Object[]> parametersList = new ArrayList<>();

        File testFile = new File(
                FlightCrewSchedulingScoreRulesXlsxTest.class.getResource(testFileName).getFile());
        try (InputStream in = new BufferedInputStream(new FileInputStream(testFile))) {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            FlightCrewSolution initialSolution = new FlightCrewSchedulingXlsxFileIO().read(testFile);

            TestFlightCrewScoreRulesReader testFileReader = new TestFlightCrewScoreRulesReader(
                    workbook, initialSolution);

            Object[] currentParameterList;
            while ((currentParameterList = testFileReader.nextTestSheetParameterList()) != null) {
                parametersList.add(currentParameterList);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException(
                    "Failed reading inputSolutionFile (" + testFile.getName() + ").", e);
        }

        return parametersList;
    }

    @Test
    public void scoreRules() {
        scoreVerifier.assertHardWeight(constraintPackage, constraintName, expectedScore.getHardScore(), solution);
        scoreVerifier.assertSoftWeight(constraintPackage, constraintName, expectedScore.getSoftScore(), solution);
    }

    private static class TestFlightCrewScoreRulesReader
            extends AbstractXlsxSolutionFileIO.AbstractXlsxReader<FlightCrewSolution> {

        private final SolutionCloner<FlightCrewSolution> solutionCloner = 
                SolutionDescriptor.buildSolutionDescriptor(FlightCrewSolution.class, FlightAssignment.class, Employee.class)
                                  .getSolutionCloner();

        private int numberOfSheets, currentTestSheetIndex;

        private final FlightCrewSolution initialSolution;

        private TestFlightCrewScoreRulesReader(XSSFWorkbook workbook,
                FlightCrewSolution initialSolution) {
            super(workbook);
            this.numberOfSheets = workbook.getNumberOfSheets();
            // Test sheets start after Flights sheet
            this.currentTestSheetIndex = workbook.getSheetIndex("Flights") + 1;
            this.initialSolution = initialSolution;
        }

        @Override
        public FlightCrewSolution read() {
            return initialSolution;
        }

        private Object[] nextTestSheetParameterList() {
            String constraintPackage;
            String constraintName;
            HardSoftLongScore expectedScore;
            FlightCrewSolution nextSheetSolution;
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
            expectedScore = HardSoftLongScore.parseScore(nextStringCell().getStringCellValue());

            nextSheetSolution = solutionCloner.cloneSolution(initialSolution);

            Map<String, Employee> employeeMap = nextSheetSolution.getEmployeeList().stream()
                                                            .collect(Collectors.toMap(Employee::getName, Function.identity()));
            
            Map<String, FlightAssignment> flightAssignmentMap = nextSheetSolution.getFlightAssignmentList()
                                                                                 .stream()
                                                                                 .collect(Collectors.toMap(f -> {
                                                                                     return f.getFlight()
                                                                                             .getFlightNumber()
                                                                                             + "_"
                                                                                             + f.getFlight()
                                                                                                .getDepartureUTCDateTime()
                                                                                             + "_"
                                                                                             + f.getIndexInFlight();
                                                                                 }, Function.identity()));

            scoreVerifier.assertHardWeight(constraintPackage, constraintName, unassignedScore.getHardScore(), nextSheetSolution);
            scoreVerifier.assertSoftWeight(constraintPackage, constraintName, unassignedScore.getSoftScore(), nextSheetSolution);

            nextRow();

            while (nextRow()) {
                String flightNumber = nextStringCell().getStringCellValue();
                LocalDateTime departureUTCDateTime = LocalDateTime.parse(nextStringCell().getStringCellValue(), DATE_TIME_FORMATTER);
                String employeeString = nextStringCell().getStringCellValue();
                String[] employeeNames = employeeString.split(", ");
                for (int i = 0; i < employeeNames.length; i++) {
                    Employee employee = employeeMap.get(employeeNames[i]);
                    if (employee == null) break;
                    FlightAssignment flightAssignment = flightAssignmentMap.get(flightNumber + "_"
                            + departureUTCDateTime + "_" + i);
                    flightAssignment.setEmployee(employee);
                    // reverse relationship
                    employee.getFlightAssignmentSet().add(flightAssignment);
                }
            }

            return new Object[] { constraintPackage, constraintName, expectedScore,
                    nextSheetSolution, testSheetName };
        }
    }
}
