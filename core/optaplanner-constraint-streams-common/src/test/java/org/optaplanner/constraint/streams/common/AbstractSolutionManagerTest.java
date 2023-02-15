package org.optaplanner.constraint.streams.common;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.api.score.stream.DefaultConstraintJustification;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.SolutionManagerTest;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.testdata.domain.TestdataConstraintProvider;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;

public abstract class AbstractSolutionManagerTest {

    private final ConstraintStreamImplType constraintStreamImplType;

    protected AbstractSolutionManagerTest(ConstraintStreamImplType constraintStreamImplType) {
        this.constraintStreamImplType = Objects.requireNonNull(constraintStreamImplType);
    }

    @Test
    void indictmentsPresentOnFreshExplanation() {
        // Create the environment.
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setConstraintProviderClass(TestdataConstraintProvider.class);
        scoreDirectorFactoryConfig.setConstraintStreamImplType(constraintStreamImplType);
        SolverConfig solverConfig = new SolverConfig();
        solverConfig.setSolutionClass(TestdataSolution.class);
        solverConfig.setEntityClassList(Collections.singletonList(TestdataEntity.class));
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);
        SolverFactory<TestdataSolution> solverFactory = SolverFactory.create(solverConfig);
        SolutionManager<TestdataSolution, SimpleScore> solutionManager =
                SolutionManagerTest.SolutionManagerSource.FROM_SOLVER_FACTORY.createSolutionManager(solverFactory);

        // Prepare the solution.
        int entityCount = 3;
        TestdataSolution solution = TestdataSolution.generateSolution(2, entityCount);
        ScoreExplanation<TestdataSolution, SimpleScore> scoreExplanation = solutionManager.explain(solution);

        // Check for expected results.
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(scoreExplanation.getScore())
                    .isEqualTo(SimpleScore.of(-entityCount));
            softly.assertThat(scoreExplanation.getConstraintMatchTotalMap())
                    .isNotEmpty();
            softly.assertThat(scoreExplanation.getIndictmentMap())
                    .isNotEmpty();
            List<DefaultConstraintJustification> constraintJustificationList = (List) scoreExplanation.getJustificationList();
            softly.assertThat(constraintJustificationList)
                    .isNotEmpty();
            softly.assertThat(scoreExplanation.getJustificationList(DefaultConstraintJustification.class))
                    .containsExactlyElementsOf(constraintJustificationList);
        });
    }

}
