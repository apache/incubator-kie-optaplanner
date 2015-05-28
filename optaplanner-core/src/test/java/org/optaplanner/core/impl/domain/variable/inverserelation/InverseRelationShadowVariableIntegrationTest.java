package org.optaplanner.core.impl.domain.variable.inverserelation;

import com.google.common.collect.Ordering;
import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.domain.variable.custom.CustomShadowVariableIntegrationScoreFunction;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowAnchor;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowEntity;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowIface;
import org.optaplanner.core.impl.testdata.domain.shadow.TestdataShadowSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InverseRelationShadowVariableIntegrationTest {

    /**
     * using planner for sorting numbers in chain (easy to check).
     * each entity has shadow variable, each shadow variable value equals "distance" from anchor
     * there are several anchors.
     * optimal solution is minimal distance from anchor over all entities.
     * (Anchor 1) <- 1 <- 2 <- 5
     * (Anchor 50) <- 50 <- 50 <- 51
     */

    @Test
    public void InverseRelationShadowVariable() {
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

        Solver solver = config.buildSolver();

        List<List<Integer>> chains = new ArrayList<List<Integer>>();
        chains.add(Arrays.asList(3, 9, 4, 2, 3, 3, 8, 3, 5, 6, 1, 7));
        chains.add(Arrays.asList(51, 50, 51, 50, 55, 50, 51, 63));
        chains.add(Arrays.asList(92, 90, 91, 91, 91, 90));
        List<TestdataShadowEntity> entities = new ArrayList<TestdataShadowEntity>();
        for (List<Integer> list : chains) {
            for (Integer num : list) {
                entities.add(TestdataShadowEntity.createNewShadowEntity(num));
            }
        }
        List<TestdataShadowAnchor> anchors = new ArrayList<TestdataShadowAnchor>();
        anchors.add(TestdataShadowAnchor.createNewShadowAnchor(0));
        anchors.add(TestdataShadowAnchor.createNewShadowAnchor(50));
        anchors.add(TestdataShadowAnchor.createNewShadowAnchor(90));

        solver.solve(TestdataShadowSolution.createChainedSortingSolution(entities, anchors));
        TestdataShadowSolution solution = (TestdataShadowSolution) solver.getBestSolution();
        anchors = solution.getAnchorList();
        for (int i = 0; i < anchors.size(); i++) {
            TestdataShadowIface start = anchors.get(i);
            List<Integer> sortedList = new ArrayList<Integer>();
            for (; start != null; ) {
                sortedList.add(start.getForwardSum());
                assertEquals(start.getValue() - anchors.get(i).getValue(), start.getForwardSum());
                start = start.getNextEntity();
            }
            assertTrue(Ordering.natural().isOrdered(sortedList));
            assertEquals(chains.get(i).size() + 1, sortedList.size()); // +1 for anchor
        }

        // --------------------------------------------------------------------------------
        // different shuffle
        chains = new ArrayList<List<Integer>>();
        chains.add(Arrays.asList(1, 7, 3,  2, 3, 9, 4, 6, 8, 3, 3, 5));
        chains.add(Arrays.asList(51, 51, 51, 50, 63, 50, 50, 55));
        chains.add(Arrays.asList(92, 91, 91, 90, 91, 90));
        entities = new ArrayList<TestdataShadowEntity>();
        for (List<Integer> list : chains) {
            for (Integer num : list) {
                entities.add(TestdataShadowEntity.createNewShadowEntity(num));
            }
        }
        anchors = new ArrayList<TestdataShadowAnchor>();
        anchors.add(TestdataShadowAnchor.createNewShadowAnchor(0));
        anchors.add(TestdataShadowAnchor.createNewShadowAnchor(50));
        anchors.add(TestdataShadowAnchor.createNewShadowAnchor(90));

        solver.solve(TestdataShadowSolution.createChainedSortingSolution(entities, anchors));
        solution = (TestdataShadowSolution) solver.getBestSolution();
        anchors = solution.getAnchorList();
        for (int i = 0; i < anchors.size(); i++) {
            TestdataShadowIface start = anchors.get(i);
            List<Integer> sortedList = new ArrayList<Integer>();
            for (; start != null; ) {
                sortedList.add(start.getForwardSum());
                assertEquals(start.getValue() - anchors.get(i).getValue(), start.getForwardSum());
                start = start.getNextEntity();
            }
            assertTrue(Ordering.natural().isOrdered(sortedList));
            assertEquals(chains.get(i).size() + 1, sortedList.size()); // +1 for anchor
        }
    }

}
