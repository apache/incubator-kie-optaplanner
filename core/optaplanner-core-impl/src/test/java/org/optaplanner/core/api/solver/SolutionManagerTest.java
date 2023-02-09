package org.optaplanner.core.api.solver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;

public class SolutionManagerTest {

    public static final SolverFactory<TestdataSolution> SOLVER_FACTORY =
            SolverFactory.createFromXmlResource("org/optaplanner/core/api/solver/testdataSolverConfig.xml");

    @ParameterizedTest
    @EnumSource(SolutionManagerSource.class)
    void updateScore(SolutionManagerSource SolutionManagerSource) {
        SolutionManager<TestdataSolution, ?> SolutionManager = SolutionManagerSource.createSolutionManager(SOLVER_FACTORY);
        assertThat(SolutionManager).isNotNull();
        TestdataSolution solution = TestdataSolution.generateSolution();
        assertThat(solution.getScore()).isNull();
        SolutionManager.update(solution);
        assertThat(solution.getScore()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(SolutionManagerSource.class)
    void explainScore(SolutionManagerSource SolutionManagerSource) {
        SolutionManager<TestdataSolution, ?> SolutionManager = SolutionManagerSource.createSolutionManager(SOLVER_FACTORY);
        assertThat(SolutionManager).isNotNull();
        TestdataSolution solution = TestdataSolution.generateSolution();
        ScoreExplanation<TestdataSolution, ?> scoreExplanation = SolutionManager.explain(solution);
        assertThat(scoreExplanation).isNotNull();
        assertSoftly(softly -> {
            softly.assertThat(scoreExplanation.getScore()).isNotNull();
            softly.assertThat(scoreExplanation.getSummary()).isNotBlank();
            softly.assertThat(scoreExplanation.getConstraintMatchTotalMap())
                    .containsOnlyKeys("org.optaplanner.core.impl.testdata.domain/testConstraint");
            softly.assertThat(scoreExplanation.getIndictmentMap())
                    .containsOnlyKeys(solution.getEntityList().toArray());

        });
    }

    public enum SolutionManagerSource {

        FROM_SOLVER_FACTORY(SolutionManager::create),
        FROM_SOLVER_MANAGER(solverFactory -> SolutionManager.create(SolverManager.create(solverFactory)));

        private final Function<SolverFactory, SolutionManager> SolutionManagerConstructor;

        SolutionManagerSource(Function<SolverFactory, SolutionManager> SolutionManagerConstructor) {
            this.SolutionManagerConstructor = SolutionManagerConstructor;
        }

        public <Solution_, Score_ extends Score<Score_>> SolutionManager<Solution_, Score_>
                createSolutionManager(SolverFactory<Solution_> solverFactory) {
            return SolutionManagerConstructor.apply(solverFactory);
        }

    }

}
