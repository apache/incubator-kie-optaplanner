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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;
import org.optaplanner.examples.cloudbalancing.optional.score.CloudBalancingConstraintProvider;
import org.optaplanner.examples.cloudbalancing.optional.score.CloudBalancingIncrementalScoreCalculator;
import org.optaplanner.examples.cloudbalancing.optional.score.CloudBalancingMapBasedEasyScoreCalculator;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;
import org.optaplanner.examples.conferencescheduling.domain.Talk;
import org.optaplanner.examples.conferencescheduling.optional.score.ConferenceSchedulingConstraintProvider;
import org.optaplanner.examples.conferencescheduling.persistence.ConferenceSchedulingXlsxFileIO;
import org.optaplanner.examples.curriculumcourse.domain.CourseSchedule;
import org.optaplanner.examples.curriculumcourse.domain.Lecture;
import org.optaplanner.examples.curriculumcourse.optional.score.CourseScheduleConstraintProvider;
import org.optaplanner.examples.flightcrewscheduling.domain.Employee;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightAssignment;
import org.optaplanner.examples.flightcrewscheduling.domain.FlightCrewSolution;
import org.optaplanner.examples.flightcrewscheduling.optional.score.FlightCrewSchedulingConstraintProvider;
import org.optaplanner.examples.flightcrewscheduling.persistence.FlightCrewSchedulingXlsxFileIO;
import org.optaplanner.examples.machinereassignment.domain.MachineReassignment;
import org.optaplanner.examples.machinereassignment.domain.MrProcessAssignment;
import org.optaplanner.examples.machinereassignment.persistence.MachineReassignmentFileIO;
import org.optaplanner.examples.machinereassignment.solver.score.MachineReassignmentConstraintProvider;
import org.optaplanner.examples.machinereassignment.solver.score.MachineReassignmentIncrementalScoreCalculator;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.domain.Queen;
import org.optaplanner.examples.nqueens.solver.score.NQueensAdvancedIncrementalScoreCalculator;
import org.optaplanner.examples.nqueens.solver.score.NQueensConstraintProvider;
import org.optaplanner.examples.nqueens.solver.score.NQueensMapBasedEasyScoreCalculator;
import org.optaplanner.examples.rocktour.domain.RockShow;
import org.optaplanner.examples.rocktour.domain.RockStandstill;
import org.optaplanner.examples.rocktour.domain.RockTourSolution;
import org.optaplanner.examples.rocktour.optional.score.RockTourConstraintProvider;
import org.optaplanner.examples.rocktour.persistence.RockTourXlsxFileIO;
import org.optaplanner.examples.taskassigning.domain.Task;
import org.optaplanner.examples.taskassigning.domain.TaskAssigningSolution;
import org.optaplanner.examples.taskassigning.domain.TaskOrEmployee;
import org.optaplanner.examples.taskassigning.solver.score.TaskAssigningConstraintProvider;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Standstill;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
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
        ProblemDescriptor[] descriptors = {
                new ProblemDescriptor("cloudBalancing", "unsolved/1600computers-4800processes.xml",
                        CloudBalancingMapBasedEasyScoreCalculator.class, CloudBalancingIncrementalScoreCalculator.class,
                        null, CloudBalancingConstraintProvider.class, CloudBalance.class, CloudProcess.class),
                new ProblemDescriptor("conferenceScheduling", "unsolved/216talks-18timeslots-20rooms.xlsx",
                        null, null, ConferenceSchedulingXlsxFileIO.class,
                        ConferenceSchedulingConstraintProvider.class, ConferenceSolution.class, Talk.class),
                new ProblemDescriptor("curriculumCourse", "unsolved/comp07.xml", null, null, null,
                        CourseScheduleConstraintProvider.class, CourseSchedule.class, Lecture.class),
                new ProblemDescriptor("flightCrewScheduling", "unsolved/875flights-7days-Europe.xlsx", null, null,
                        FlightCrewSchedulingXlsxFileIO.class, FlightCrewSchedulingConstraintProvider.class,
                        FlightCrewSolution.class, FlightAssignment.class, Employee.class),
                new ProblemDescriptor("machineReassignment", "import/model_b_10.txt", null,
                        MachineReassignmentIncrementalScoreCalculator.class, MachineReassignmentFileIO.class,
                        MachineReassignmentConstraintProvider.class, MachineReassignment.class,
                        MrProcessAssignment.class),
                new ProblemDescriptor("nQueens", "unsolved/256queens.xml", NQueensMapBasedEasyScoreCalculator.class,
                        NQueensAdvancedIncrementalScoreCalculator.class, null, NQueensConstraintProvider.class,
                        NQueens.class, Queen.class),
                new ProblemDescriptor("rockTour", "unsolved/47shows.xlsx", null, null,
                        RockTourXlsxFileIO.class, RockTourConstraintProvider.class, RockTourSolution.class,
                        RockShow.class, RockStandstill.class),
                new ProblemDescriptor("taskAssigning", "unsolved/500tasks-20employees.xml", null, null, null,
                        TaskAssigningConstraintProvider.class, TaskAssigningSolution.class, TaskOrEmployee.class,
                        Task.class),
                new ProblemDescriptor("vehicleRouting", "import/belgium/basic/air/belgium-n2750-k55.vrp",
                        VehicleRoutingEasyScoreCalculator.class, VehicleRoutingIncrementalScoreCalculator.class,
                        VehicleRoutingFileIO.class, VehicleRoutingConstraintProvider.class,
                        VehicleRoutingSolution.class, Standstill.class, Customer.class, TimeWindowedCustomer.class)
        };
        Map<String, Object> model = new HashMap<>();
        model.put("benchmarkDescriptors", descriptors);
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "org/optaplanner/examples/app/benchmark/scoreDirectorFactoryBenchmarkConfigTemplate.xml.ftl", model);
        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark();
        benchmark.benchmarkAndShowReportInBrowser();
    }

    public static final class ProblemDescriptor {

        private final String exampleId;
        private final String solutionFileIoClass;
        private final String inputSolutionFile;
        private final String drlFile;
        private final String easyScoreCalculator;
        private final String incrementalScoreCalculator;
        private final String constraintProvider;
        private final String solutionClass;
        private final Set<String> entityClasses;

        public <Solution_> ProblemDescriptor(String exampleId, String inputSolutionFile,
                Class<? extends EasyScoreCalculator<Solution_>> easyScoreCalculatorClass,
                Class<? extends IncrementalScoreCalculator<Solution_>> incrementalScoreCalculatorClass,
                Class<? extends SolutionFileIO<Solution_>> solutionFileIoClass,
                Class<? extends ConstraintProvider> constraintProviderClass, Class<Solution_> solutionClass,
                Class<?>... entityClasses) {
            this.exampleId = exampleId;
            this.solutionFileIoClass = solutionFileIoClass == null ? null : solutionFileIoClass.getCanonicalName();
            String parentFolder = exampleId.toLowerCase();
            String fullInputSolutionPath = "data/" + parentFolder + "/" + inputSolutionFile;
            if (!new File(fullInputSolutionPath).exists()) {
                throw new IllegalArgumentException("No input solution (" + inputSolutionFile + ") for example (" + exampleId + ").");
            }
            this.inputSolutionFile = fullInputSolutionPath;
            String fullDrlPath = "org/optaplanner/examples/" + parentFolder + "/solver/" + exampleId + "ScoreRules.drl";
            try (InputStream stream = getClass().getResourceAsStream("/" + fullDrlPath)) {
                if (stream == null) {
                    throw new IllegalArgumentException("No DRL (" + fullDrlPath + ") for example (" + exampleId + ").");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("No DRL (" + fullDrlPath + ") for example (" + exampleId + ").", e);
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
            this.solutionClass = solutionClass.getCanonicalName();
            this.entityClasses = Stream.of(entityClasses)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.toSet());
        }

        public String getExampleId() {
            return exampleId;
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

        public String getSolutionClass() {
            return solutionClass;
        }

        public Set<String> getEntityClasses() {
            return entityClasses;
        }
    }
}
