package org.optaplanner.core.impl.score;

import java.util.Objects;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.solver.SolutionManager;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @deprecated Use {@link org.optaplanner.core.impl.solver.DefaultSolutionManager} instead.
 */
@Deprecated(forRemoval = true)
public final class DefaultScoreManager<Solution_, Score_ extends Score<Score_>>
        implements ScoreManager<Solution_, Score_> {

    private final SolutionManager<Solution_, Score_> solutionManager;

    public DefaultScoreManager(SolutionManager<Solution_, Score_> solutionManager) {
        this.solutionManager = Objects.requireNonNull(solutionManager);
    }

    @Override
    public Score_ updateScore(Solution_ solution) {
        return solutionManager.updateScore(solution);
    }

    @Override
    public String getSummary(Solution_ solution) {
        return solutionManager.getSummary(solution);
    }

    @Override
    public ScoreExplanation<Solution_, Score_> explainScore(Solution_ solution) {
        return solutionManager.explainScore(solution);
    }
}
