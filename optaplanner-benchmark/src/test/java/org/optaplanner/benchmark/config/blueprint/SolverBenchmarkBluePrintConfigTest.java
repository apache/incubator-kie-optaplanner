package org.optaplanner.benchmark.config.blueprint;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SolverBenchmarkBluePrintConfigTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void throwIllegalArgumentException_WhenValidateOnUnassignedSolverBenchmarkBluePrintType() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The solverBenchmarkBluePrint must have a solverBenchmarkBluePrintType (");

        SolverBenchmarkBluePrintConfig config = new SolverBenchmarkBluePrintConfig();
        config.validate();
    }
}
