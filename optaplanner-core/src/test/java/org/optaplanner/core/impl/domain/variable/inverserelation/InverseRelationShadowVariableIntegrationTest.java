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
        /*chains.add(Arrays.asList(1, 2, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9));
        chains.add(Arrays.asList(50, 50, 50, 51, 51, 51, 55, 63));
        chains.add(Arrays.asList(90, 90, 91, 91, 91, 92));*/
        chains.add(Arrays.asList(3, 9, 4, 2, 3, 3, 8, 3, 5, 6, 1, 7));
        chains.add(Arrays.asList(51, 50, 51, 50, 55, 50, 51, 63));
        chains.add(Arrays.asList(92, 90, 91, 91, 91, 90));
        List<TestdataShadowEntity> numbers = new ArrayList<TestdataShadowEntity>();
        for (List<Integer> list : chains) {
            for (Integer num : list) {
                numbers.add(TestdataShadowEntity.createNewShadowEntity(num));
            }
        }
        List<TestdataShadowAnchor> startingPoints = new ArrayList<TestdataShadowAnchor>();
        startingPoints.add(TestdataShadowAnchor.createNewShadowAnchor(0));
        startingPoints.add(TestdataShadowAnchor.createNewShadowAnchor(50));
        startingPoints.add(TestdataShadowAnchor.createNewShadowAnchor(90));


        solver.solve(TestdataShadowSolution.createChainedSortingSolution(numbers, startingPoints));
        TestdataShadowSolution solution = (TestdataShadowSolution) solver.getBestSolution();
        startingPoints = solution.getAnchorList();
        for (int i = 0; i < startingPoints.size(); i++) {
            TestdataShadowIface start = startingPoints.get(i);
            List<Integer> sortedList = new ArrayList<Integer>();
            for (; start != null; ) {
                sortedList.add(start.getValue());
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
        numbers = new ArrayList<TestdataShadowEntity>();
        for (List<Integer> list : chains) {
            for (Integer num : list) {
                numbers.add(TestdataShadowEntity.createNewShadowEntity(num));
            }
        }
        startingPoints = new ArrayList<TestdataShadowAnchor>();
        startingPoints.add(TestdataShadowAnchor.createNewShadowAnchor(0));
        startingPoints.add(TestdataShadowAnchor.createNewShadowAnchor(50));
        startingPoints.add(TestdataShadowAnchor.createNewShadowAnchor(90));

        solver.solve(TestdataShadowSolution.createChainedSortingSolution(numbers, startingPoints));
        solution = (TestdataShadowSolution) solver.getBestSolution();
        startingPoints = solution.getAnchorList();
        for (int i = 0; i < startingPoints.size(); i++) {
            TestdataShadowIface start = startingPoints.get(i);
            List<Integer> sortedList = new ArrayList<Integer>();
            for (; start != null; ) {
                sortedList.add(start.getValue());
                start = start.getNextEntity();
            }
            assertTrue(Ordering.natural().isOrdered(sortedList));
            assertEquals(chains.get(i).size() + 1, sortedList.size()); // +1 for anchor
        }
    }

}
