package org.optaplanner.constraint.streams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.testdata.domain.chained.shadow.TestdataShadowingChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.shadow.TestdataShadowingChainedEntity;
import org.optaplanner.core.impl.testdata.domain.chained.shadow.TestdataShadowingChainedSolution;

class FactChangePropagationTest {

    private static final String VALUE_CODE = "v1";
    private Solver<TestdataShadowingChainedSolution> solver = buildSolver();

    /**
     * Tests if Drools try to modify a {@link PlanningEntity} before its shadow variables are updated.
     */
    @Test
    void delayedFactChangePropagation() {
        TestdataShadowingChainedEntity entity = new TestdataShadowingChainedEntity("e1");
        TestdataShadowingChainedAnchor value = new TestdataShadowingChainedAnchor(VALUE_CODE);
        TestdataShadowingChainedSolution inputProblem = new TestdataShadowingChainedSolution();
        inputProblem.setChainedAnchorList(Arrays.asList(value));
        inputProblem.setChainedEntityList(Arrays.asList(entity));

        TestdataShadowingChainedSolution solution = solver.solve(inputProblem);
        TestdataShadowingChainedEntity solvedEntity = solution.getChainedEntityList().get(0);
        assertThat(solvedEntity.getChainedObject()).isNotNull();
        assertThat(solvedEntity.getAnchor().getCode()).isEqualTo(VALUE_CODE);
        assertThat(solution.getScore().isFeasible()).isTrue();
    }

    private Solver<TestdataShadowingChainedSolution> buildSolver() {
        SolverConfig solverConfig = new SolverConfig()
                .withEntityClasses(TestdataShadowingChainedEntity.class)
                .withSolutionClass(TestdataShadowingChainedSolution.class)
                .withConstraintProviderClass(ChainedEntityConstraintProvider.class)
                .withPhases(new ConstructionHeuristicPhaseConfig());

        SolverFactory<TestdataShadowingChainedSolution> solverFactory = SolverFactory.create(solverConfig);
        return solverFactory.buildSolver();
    }

    public static class ChainedEntityConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
            return new Constraint[] {
                    anchorCannotBeNull(constraintFactory)
            };
        }

        private Constraint anchorCannotBeNull(ConstraintFactory constraintFactory) {
            return constraintFactory.forEach(TestdataShadowingChainedEntity.class)
                    /*
                     * The getCode() is here just to trigger NPE if the filter's predicate has been called before
                     * the AnchorVariableListener has updated the anchor.
                     */
                    .filter(testdataShadowingChainedEntity -> "v1".equals(testdataShadowingChainedEntity.getAnchor().getCode()))
                    .penalize("anchorCannotBeNull", SimpleScore.ONE);
        }
    }
}
