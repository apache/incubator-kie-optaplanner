package org.optaplanner.core.impl.heuristic.selector.common.decorator;

import org.junit.Test;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.domain.valuerange.buildin.composite.NullableCountableValueRange;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataCompositeCountableEntity;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataCompositeCountableNullableFilteredEntity;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataIntegerRangeSolution;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ReinitializeVariableEntityFilterIntegrationTest {

    /**
     * test if construction heuristics respects filtering
     */

    @Test
    public void reinitializeVariableEntityFilter() {
        SolverConfig config = new SolverConfig();
        config.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);
        config.setSolutionClass(TestdataIntegerRangeSolution.class);
        config.setEntityClassList(Collections.<Class<?>>singletonList(TestdataCompositeCountableNullableFilteredEntity.class));

        config.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig());
        config.getScoreDirectorFactoryConfig().setEasyScoreCalculatorClass(ReinitializeVariableEntityFilterIntegrationScoreFunction.class);


        config.setPhaseConfigList(new ArrayList<PhaseConfig>());
        ConstructionHeuristicPhaseConfig chConfig = new ConstructionHeuristicPhaseConfig();
        chConfig.setConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT);
        config.getPhaseConfigList().add(chConfig);

        NullableCountableValueRange <Integer> rangeX =
                new NullableCountableValueRange<Integer>(ValueRangeFactory.createIntValueRange(0, 2));

        TestdataCompositeCountableEntity x = new TestdataCompositeCountableNullableFilteredEntity();
        x.setValueRange(rangeX);
        x.setValue(null);
        TestdataIntegerRangeSolution problem = new TestdataIntegerRangeSolution();
        problem.setEntities(Collections.singletonList(x));

        Solver solver = config.buildSolver();
        solver.solve(problem);
        TestdataIntegerRangeSolution solution = (TestdataIntegerRangeSolution) solver.getBestSolution();

        assertEquals(Integer.MIN_VALUE, solution.getScore().getScore());
    }

}
