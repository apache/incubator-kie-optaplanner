/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.optional.score.CloudBalancingConstraintProvider;
import org.optaplanner.examples.cloudbalancing.optional.score.CloudBalancingIncrementalScoreCalculator;
import org.optaplanner.examples.cloudbalancing.optional.score.CloudBalancingMapBasedEasyScoreCalculator;
import org.optaplanner.examples.conferencescheduling.optional.score.ConferenceSchedulingConstraintProvider;
import org.optaplanner.examples.conferencescheduling.persistence.ConferenceSchedulingXlsxFileIO;
import org.optaplanner.examples.curriculumcourse.domain.CourseSchedule;
import org.optaplanner.examples.curriculumcourse.optional.score.CourseScheduleConstraintProvider;
import org.optaplanner.examples.flightcrewscheduling.optional.score.FlightCrewSchedulingConstraintProvider;
import org.optaplanner.examples.flightcrewscheduling.persistence.FlightCrewSchedulingXlsxFileIO;
import org.optaplanner.examples.machinereassignment.persistence.MachineReassignmentFileIO;
import org.optaplanner.examples.machinereassignment.solver.score.MachineReassignmentConstraintProvider;
import org.optaplanner.examples.machinereassignment.solver.score.MachineReassignmentIncrementalScoreCalculator;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.solver.score.NQueensAdvancedIncrementalScoreCalculator;
import org.optaplanner.examples.nqueens.solver.score.NQueensConstraintProvider;
import org.optaplanner.examples.nqueens.solver.score.NQueensMapBasedEasyScoreCalculator;
import org.optaplanner.examples.rocktour.optional.score.RockTourConstraintProvider;
import org.optaplanner.examples.rocktour.persistence.RockTourXlsxFileIO;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.solver.score.TaskAssigningConstraintProvider;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.optional.score.VehicleRoutingConstraintProvider;
import org.optaplanner.examples.vehiclerouting.persistence.VehicleRoutingFileIO;
import org.optaplanner.examples.vehiclerouting.solver.score.VehicleRoutingEasyScoreCalculator;
import org.optaplanner.examples.vehiclerouting.solver.score.VehicleRoutingIncrementalScoreCalculator;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

// TODO include cheaptime
// TODO include coachshuttlegathering
// TODO include dinnerparty
// TODO include examination
// TODO include investment
// TODO include meetingscheduling
// TODO include nurserostering
// TODO include pas
// TODO include projectjobscheduling
// TODO include scrabble
// TODO include tennis
// TODO include travelingtournament
// TODO include tsp
public class ScoreDirectorFactoryBenchmarkApp {

    public static void main(String... args) {
        BenchmarkDescriptor[] descriptors = {
                new BenchmarkDescriptor("cloudbalancing", CloudBalance.class, null,
                        "unsolved/400computers-1200processes.xml", "cloudBalancingScoreRules.drl",
                        CloudBalancingMapBasedEasyScoreCalculator.class,
                        CloudBalancingIncrementalScoreCalculator.class,
                        CloudBalancingConstraintProvider.class),
                new BenchmarkDescriptor("conferencescheduling", null, ConferenceSchedulingXlsxFileIO.class,
                        "unsolved/72talks-12timeslots-10rooms.xlsx", "conferenceSchedulingScoreRules.drl",
                        null, null, ConferenceSchedulingConstraintProvider.class),
                new BenchmarkDescriptor("curriculumcourse", CourseSchedule.class, null,
                        "unsolved/comp08.xml", "curriculumCourseScoreRules.drl",
                        null, null, CourseScheduleConstraintProvider.class),
                new BenchmarkDescriptor("flightcrewscheduling", null, FlightCrewSchedulingXlsxFileIO.class,
                        "unsolved/175flights-7days-US.xlsx", "flightCrewSchedulingScoreRules.drl",
                        null, null, FlightCrewSchedulingConstraintProvider.class),
                new BenchmarkDescriptor("machinereassignment", null, MachineReassignmentFileIO.class,
                        "import/model_b_2.txt", "machineReassignmentScoreRules.drl",
                        null, MachineReassignmentIncrementalScoreCalculator.class,
                        MachineReassignmentConstraintProvider.class),
                new BenchmarkDescriptor("nqueens", NQueens.class, null,
                        "unsolved/256queens.xml", "nQueensScoreRules.drl",
                        NQueensMapBasedEasyScoreCalculator.class, NQueensAdvancedIncrementalScoreCalculator.class,
                        NQueensConstraintProvider.class),
                new BenchmarkDescriptor("rocktour", null, RockTourXlsxFileIO.class,
                        "unsolved/47shows.xlsx", "rockTourScoreRules.drl", null, null,
                        RockTourConstraintProvider.class),
                new BenchmarkDescriptor("taskassigning", TaskAssigningSolution.class, null,
                        "unsolved/100tasks-5employees.xml", "taskAssigningScoreRules.drl",
                        null, null, TaskAssigningConstraintProvider.class),
                new BenchmarkDescriptor("vehiclerouting", VehicleRoutingSolution.class, VehicleRoutingFileIO.class,
                        "import/vrpweb/timewindowed/air/Solomon_025_C101.vrp", "vehicleRoutingScoreRules.drl",
                        VehicleRoutingEasyScoreCalculator.class, VehicleRoutingIncrementalScoreCalculator.class,
                        VehicleRoutingConstraintProvider.class)
        };
        Map<String, Object> model = new HashMap<>();
        model.put("benchmarkDescriptors", descriptors);
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "org/optaplanner/examples/app/benchmark/scoreDirectorFactoryBenchmarkConfigTemplate.xml.ftl", model);
        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark();
        benchmark.benchmarkAndShowReportInBrowser();
    }

    public static final class BenchmarkDescriptor {

        private final String exampleId;
        private final String solutionFileIoClass;
        private final String xStreamAnnotatedClass;
        private final String inputSolutionFile;
        private final String drlFile;
        private final String easyScoreCalculator;
        private final String incrementalScoreCalculator;
        private final String constraintProvider;

        public <Solution_> BenchmarkDescriptor(String exampleId, Class<?> xStreamAnnotatedClass,
                Class<? extends SolutionFileIO> solutionFileIoClass, String inputSolutionFile, String drlFileName,
                Class<? extends EasyScoreCalculator<Solution_>> easyScoreCalculatorClass,
                Class<? extends IncrementalScoreCalculator<Solution_>> incrementalScoreCalculatorClass,
                Class<? extends ConstraintProvider> constraintProviderClass) {
            this.exampleId = exampleId;
            if (solutionFileIoClass == null && xStreamAnnotatedClass == null) {
                throw new IllegalArgumentException("Example (" + exampleId +
                        ") provides neither solutionFileIoClass nor xStreamAnnotatedClass.");
            }
            this.solutionFileIoClass = solutionFileIoClass == null ? null : solutionFileIoClass.getCanonicalName();
            this.xStreamAnnotatedClass = xStreamAnnotatedClass == null ? null : xStreamAnnotatedClass.getCanonicalName();
            String fullInputSolutionPath = "data/" + exampleId + "/" + inputSolutionFile;
            if (!new File(fullInputSolutionPath).exists()) {
                throw new IllegalArgumentException("No input solution (" + inputSolutionFile + ") for example (" + exampleId + ").");
            }
            this.inputSolutionFile = fullInputSolutionPath;
            String fullDrlPath = "org/optaplanner/examples/" + exampleId + "/solver/" + drlFileName;
            try (InputStream stream = getClass().getResourceAsStream("/" + fullDrlPath)) {
                if (stream == null) {
                    throw new IllegalArgumentException("No DRL (" + drlFileName + ") for example (" + exampleId + ").");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("No DRL (" + drlFileName + ") for example (" + exampleId + ").", e);
            }
            this.drlFile = fullDrlPath;
            this.easyScoreCalculator = easyScoreCalculatorClass == null ?
                    null :
                    easyScoreCalculatorClass.getCanonicalName();
            this.incrementalScoreCalculator = incrementalScoreCalculatorClass == null ?
                    null :
                    incrementalScoreCalculatorClass.getCanonicalName();
            this.constraintProvider = constraintProviderClass == null ?
                    null :
                    constraintProviderClass.getCanonicalName();
        }

        public String getExampleId() {
            return exampleId;
        }

        public String getXStreamAnnotatedClass() {
            return xStreamAnnotatedClass;
        }

        public String getSolutionFileIoClass() {
            return solutionFileIoClass;
        }

        public String getInputSolutionFile() {
            return inputSolutionFile;
        }

        public String getDrlFile() {
            return drlFile;
        }

        public String getEasyScoreCalculator() {
            return easyScoreCalculator;
        }

        public String getIncrementalScoreCalculator() {
            return incrementalScoreCalculator;
        }

        public String getConstraintProvider() {
            return constraintProvider;
        }
    }
}
