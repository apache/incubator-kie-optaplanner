package org.optaplanner.core.impl.domain.valuerange.buildin.composite;

import org.junit.Test;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.domain.valuerange.buildin.primint.IntValueRange;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataCompositeCountableEntity;
import org.optaplanner.core.impl.testdata.domain.valuerange.TestdataIntegerRangeSolution;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NullableCountableValueRangeIntegrationTest {

    @Test
    public void nullableValueRange() {
        IntValueRange countableValueRange = new IntValueRange(0, 10);
        NullableCountableValueRange<Integer> nullableCountableValueRange = new NullableCountableValueRange<Integer>(countableValueRange);
        TestdataCompositeCountableEntity entity = new TestdataCompositeCountableEntity();
        entity.setValue(0);
        entity.setValueRange(nullableCountableValueRange);
        TestdataIntegerRangeSolution problem = new TestdataIntegerRangeSolution();
        problem.setEntities(Arrays.asList(entity));

        SolverConfig config = new SolverConfig();
        config.setSolutionClass(TestdataIntegerRangeSolution.class);
        config.setEntityClassList(Arrays.<Class<?>>asList(TestdataCompositeCountableEntity.class));

        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setScoreDefinitionType(ScoreDefinitionType.SIMPLE);
        scoreDirectorFactoryConfig.setEasyScoreCalculatorClass(NullableCountableValueRangeIntegrationScoreFunction.class);
        config.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);

        config.setPhaseConfigList(new ArrayList<PhaseConfig>());

        LocalSearchPhaseConfig phaseConfig = new LocalSearchPhaseConfig();
        phaseConfig.setAcceptorConfig(new AcceptorConfig());
        phaseConfig.getAcceptorConfig().setAcceptorTypeList(Arrays.asList(AcceptorType.LATE_ACCEPTANCE));
        phaseConfig.setForagerConfig(new LocalSearchForagerConfig());
        phaseConfig.getForagerConfig().setAcceptedCountLimit(2);
        config.getPhaseConfigList().add(phaseConfig);

        config.getPhaseConfigList().get(0).setTerminationConfig(new TerminationConfig());
        config.getPhaseConfigList().get(0).getTerminationConfig().setStepCountLimit(100);

        config.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);

        Solver solver = config.buildSolver();
        solver.solve(problem);
        Solution solution = solver.getBestSolution();

        assertEquals(1, ((SimpleScore)solution.getScore()).getScore()); // null was picked
    }

}
