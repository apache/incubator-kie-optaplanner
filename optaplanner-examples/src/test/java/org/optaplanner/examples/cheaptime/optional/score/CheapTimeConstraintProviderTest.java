package org.optaplanner.examples.cheaptime.optional.score;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.cheaptime.domain.CheapTimeSolution;
import org.optaplanner.examples.cheaptime.domain.Machine;
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

    private static final Resource RESOURCE_0 = new Resource(0);
    private static final Resource RESOURCE_1 = new Resource(1);
    private static final Resource RESOURCE_2 = new Resource(2);
    private static final Machine MACHINE_O = new Machine();
    private static final Task TASK_0 = new Task(0, PERIOD_1, PERIOD_2, 1, 1);

    private final ConstraintVerifier<CheapTimeConstraintProvider, CheapTimeSolution> constraintVerifier =
            ConstraintVerifier.build(new CheapTimeConstraintProvider(), CheapTimeSolution.class, TaskAssignment.class);

    @Test
    void startTimeLimitsFrom() {
        TaskAssignment taskAssignment = new TaskAssignment();
    }

}
