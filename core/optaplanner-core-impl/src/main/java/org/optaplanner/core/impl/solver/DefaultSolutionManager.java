package org.optaplanner.core.impl.solver;

import java.util.function.Function;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.DefaultScoreExplanation;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public final class DefaultSolutionManager<Solution_, Score_ extends Score<Score_>>
        implements SolutionManager<Solution_, Score_> {

    private final InnerScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory;

    public <ProblemId_> DefaultSolutionManager(SolverManager<Solution_, ProblemId_> solverManager) {
        this(((DefaultSolverManager<Solution_, ProblemId_>) solverManager).getSolverFactory());
    }

    public DefaultSolutionManager(SolverFactory<Solution_> solverFactory) {
        this.scoreDirectorFactory = ((DefaultSolverFactory<Solution_>) solverFactory).getScoreDirectorFactory();
    }

    public InnerScoreDirectorFactory<Solution_, Score_> getScoreDirectorFactory() {
        return scoreDirectorFactory;
    }

    @Override
    public Score_ updateScore(Solution_ solution, boolean updateShadowVariables) {
        return callScoreDirector(solution, InnerScoreDirector::calculateScore, updateShadowVariables, false);
    }

    @Override
    public String getSummary(Solution_ solution, boolean updateShadowVariables) {
        return explainScore(solution, updateShadowVariables).getSummary();
    }

    @Override
    public ScoreExplanation<Solution_, Score_> explainScore(Solution_ solution, boolean updateShadowVariables) {
        return callScoreDirector(solution, DefaultScoreExplanation::new, updateShadowVariables, true);
    }

    private <Result_> Result_ callScoreDirector(Solution_ solution,
            Function<InnerScoreDirector<Solution_, Score_>, Result_> function, boolean updateShadowVariables,
            boolean enableConstraintMatch) {
        try (InnerScoreDirector<Solution_, Score_> scoreDirector =
                scoreDirectorFactory.buildScoreDirector(false, enableConstraintMatch)) {
            boolean constraintMatchEnabled = scoreDirector.isConstraintMatchEnabled();
            if (enableConstraintMatch && !constraintMatchEnabled) {
                throw new IllegalStateException("When constraintMatchEnabled (" + constraintMatchEnabled
                        + ") is disabled, this method should not be called.");
            }
            if (updateShadowVariables) {
                SolutionDescriptor<Solution_> solutionDescriptor = scoreDirector.getSolutionDescriptor();
                solutionDescriptor.visitAllProblemFacts(solution, scoreDirector::beforeProblemFactAdded);
                solutionDescriptor.visitAllEntities(solution, scoreDirector::beforeEntityAdded);
            }
            scoreDirector.setWorkingSolution(solution); // Init the ScoreDirector first, else NPEs may be thrown.
            if (updateShadowVariables) {
                SolutionDescriptor<Solution_> solutionDescriptor = scoreDirector.getSolutionDescriptor();
                solutionDescriptor.visitAllProblemFacts(solution, scoreDirector::afterProblemFactAdded);
                solutionDescriptor.visitAllEntities(solution, scoreDirector::afterEntityAdded);
                scoreDirector.triggerVariableListeners();
            }
            return function.apply(scoreDirector);
        }
    }
}
