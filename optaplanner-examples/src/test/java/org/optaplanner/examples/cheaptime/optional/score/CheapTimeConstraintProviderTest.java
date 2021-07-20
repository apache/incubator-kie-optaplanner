package org.optaplanner.examples.cheaptime.optional.score;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.cheaptime.domain.CheapTimeSolution;
import org.optaplanner.examples.cheaptime.domain.Machine;
import org.optaplanner.examples.cheaptime.domain.MachineCapacity;
import org.optaplanner.examples.cheaptime.domain.Period;
import org.optaplanner.examples.cheaptime.domain.Resource;
import org.optaplanner.examples.cheaptime.domain.Task;
import org.optaplanner.examples.cheaptime.domain.TaskAssignment;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;
import org.optaplanner.examples.cloudbalancing.optional.score.CloudBalancingConstraintProvider;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

class CheapTimeConstraintProviderTest {

    private static final Period PERIOD_0 = new Period(0, 1);
    private static final Period PERIOD_1 = new Period(1, 2);
    private static final Period PERIOD_2 = new Period(2, 3);
    private static final Period PERIOD_3 = new Period(3, 4);

    private static final Resource RESOURCE_0 = new Resource(0);
    private static final Resource RESOURCE_1 = new Resource(1);
    private static final Resource RESOURCE_2 = new Resource(2);
    private static final MachineCapacity CAPACITY_MACHINE0_RESOURCE0 = new MachineCapacity(0, RESOURCE_0, 1);
    private static final MachineCapacity CAPACITY_MACHINE0_RESOURCE1 = new MachineCapacity(1, RESOURCE_1, 2);
    private static final MachineCapacity CAPACITY_MACHINE0_RESOURCE2 = new MachineCapacity(2, RESOURCE_2, 3);
    private static final Machine MACHINE_O = new Machine(0, 1, 1,
            CAPACITY_MACHINE0_RESOURCE0,
            CAPACITY_MACHINE0_RESOURCE1,
            CAPACITY_MACHINE0_RESOURCE2);
    private static final Task TASK_0 = new Task(0, PERIOD_0, PERIOD_1, 1, 1);
    private static final Task TASK_1 = new Task(1, PERIOD_1, PERIOD_2, 1, 2);
    private static final Task TASK_2 = new Task(2, PERIOD_1, PERIOD_3, 1, 3);

    private final ConstraintVerifier<CheapTimeConstraintProvider, CheapTimeSolution> constraintVerifier =
            ConstraintVerifier.build(new CheapTimeConstraintProvider(), CheapTimeSolution.class, TaskAssignment.class);

    @Test
    void startTimeLimitsFrom() {
        // Actual task assignment falls on the minimum prescribed by the task.
        TaskAssignment correctTaskAssignment1 = new TaskAssignment(TASK_0, MACHINE_O, PERIOD_0);
        // Actual task assignment falls past the minimum prescribed by the task.
        TaskAssignment correctTaskAssignment2 = new TaskAssignment(TASK_1, MACHINE_O, PERIOD_2);
        // Actual assignment start is before the minimum prescribed by the task.
        TaskAssignment wrongTaskAssignment = new TaskAssignment(TASK_2, MACHINE_O, PERIOD_0);
        constraintVerifier.verifyThat(CheapTimeConstraintProvider::startTimeLimitsFrom)
                .given(correctTaskAssignment1, correctTaskAssignment2, wrongTaskAssignment)
                .penalizesBy(1); // Wrong task assignment is penalized by one period.
    }

    @Test
    void startTimeLimitsTo() {
        // Actual task assignment falls on the maximum prescribed by the task.
        TaskAssignment correctTaskAssignment1 = new TaskAssignment(TASK_2, MACHINE_O, PERIOD_3);
        // Actual task assignment falls before the maximum prescribed by the task.
        TaskAssignment correctTaskAssignment2 = new TaskAssignment(TASK_1, MACHINE_O, PERIOD_1);
        // Actual assignment start is after the maximum prescribed by the task.
        TaskAssignment wrongTaskAssignment = new TaskAssignment(TASK_0, MACHINE_O, PERIOD_2);
        constraintVerifier.verifyThat(CheapTimeConstraintProvider::startTimeLimitsTo)
                .given(correctTaskAssignment1, correctTaskAssignment2, wrongTaskAssignment)
                .penalizesBy(1); // Wrong task assignment is penalized by one period.
    }

}
