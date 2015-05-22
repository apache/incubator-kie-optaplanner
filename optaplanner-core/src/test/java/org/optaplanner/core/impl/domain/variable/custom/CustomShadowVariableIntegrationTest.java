package org.optaplanner.core.impl.domain.variable.custom;

import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowAnchor;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowEntity;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowIface;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CustomShadowVariableIntegrationTest {

    @Test
    public void customShadowVariable() {
        SolverConfig config = new SolverConfig();
        config.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);
        config.setSolutionClass(TestdataShadowSolution.class);
        config.setEntityClassList(Arrays.<Class<?>>asList(TestdataShadowIface.class, TestdataShadowEntity.class));

        config.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig());
        config.getScoreDirectorFactoryConfig().setScoreDefinitionType(ScoreDefinitionType.SIMPLE);
        config.getScoreDirectorFactoryConfig().setEasyScoreCalculatorClass(CustomShadowVariableIntegrationScoreFunction.class);

        config.setPhaseConfigList(new ArrayList<PhaseConfig>());

        ConstructionHeuristicPhaseConfig chConfig = new ConstructionHeuristicPhaseConfig();
        chConfig.setConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT_DECREASING);
        config.getPhaseConfigList().add(chConfig);

        List<TestdataShadowAnchor> anchors = Arrays.asList(TestdataShadowAnchor.createNewShadowAnchor(0));
        List<TestdataShadowEntity> entities = new ArrayList<TestdataShadowEntity>();
        for (int i = 1; i <= 10; i++) {
            entities.add(TestdataShadowEntity.createNewShadowEntity(i));
        }
        Solver solver = config.buildSolver();
        solver.solve(TestdataShadowSolution.createChainedSortingSolution(entities, anchors));

        TestdataShadowSolution solution = (TestdataShadowSolution) solver.getBestSolution();
        List<TestdataShadowAnchor> startingPoints = solution.getAnchorList();
        for (TestdataShadowAnchor startingPoint : startingPoints) {
            TestdataShadowIface start = startingPoint;
            for (; start != null; ) {
                /**
                 * in each entity should be forward sum equal to entity value;
                 */
                assertEquals(start.getValue(), start.getForwardSum());
                start = start.getNextEntity();
            }
        }
    }

}
