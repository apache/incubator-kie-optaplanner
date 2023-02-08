package org.optaplanner.core.impl.score;

import java.util.function.Function;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.core.impl.solver.DefaultSolverFactory;
import org.optaplanner.core.impl.solver.DefaultSolverManager;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public final class DefaultScoreManager<Solution_, Score_ extends Score<Score_>>
        implements ScoreManager<Solution_, Score_> {

    private final InnerScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory;

    public <ProblemId_> DefaultScoreManager(SolverManager<Solution_, ProblemId_> solverManager) {
        this(((DefaultSolverManager<Solution_, ProblemId_>) solverManager).getSolverFactory());
    }

    public DefaultScoreManager(SolverFactory<Solution_> solverFactory) {
        this.scoreDirectorFactory = ((DefaultSolverFactory<Solution_>) solverFactory).getScoreDirectorFactory();
    }

    public InnerScoreDirectorFactory<Solution_, Score_> getScoreDirectorFactory() {
        return scoreDirectorFactory;
    }

    @Override
    public Score_ updateScore(Solution_ solution, boolean triggerVariableListeners) {
        return callScoreDirector(solution, InnerScoreDirector::calculateScore, triggerVariableListeners, false);
    }

    @Override
    public void triggerVariableListeners(Solution_ solution) {
        callScoreDirector(solution, s -> null, true, false);
    }

    private void innerTriggerVariableListeners(InnerScoreDirector<Solution_, Score_> innerScoreDirector, Solution_ solution) {
        SolutionDescriptor<Solution_> solutionDescriptor = innerScoreDirector.getSolutionDescriptor();
        solutionDescriptor.visitAllProblemFacts(solution, entity -> {
            innerScoreDirector.beforeProblemFactAdded(entity);
            innerScoreDirector.afterProblemFactAdded(entity);
        });
        solutionDescriptor.visitAllEntities(solution, fact -> {
            innerScoreDirector.beforeEntityAdded(fact);
            innerScoreDirector.afterEntityAdded(fact);
        });
        innerScoreDirector.triggerVariableListeners();
    }

    @Override
    public String getSummary(Solution_ solution, boolean triggerVariableListeners) {
        return explainScore(solution, triggerVariableListeners).getSummary();
    }

    @Override
    public ScoreExplanation<Solution_, Score_> explainScore(Solution_ solution, boolean triggerVariableListeners) {
        return callScoreDirector(solution, DefaultScoreExplanation::new, triggerVariableListeners, true);
    }

    private <Result_> Result_ callScoreDirector(Solution_ solution,
            Function<InnerScoreDirector<Solution_, Score_>, Result_> function, boolean triggerVariableListeners,
            boolean enableConstraintMatch) {
        try (InnerScoreDirector<Solution_, Score_> scoreDirector =
                scoreDirectorFactory.buildScoreDirector(false, enableConstraintMatch)) {
            boolean constraintMatchEnabled = scoreDirector.isConstraintMatchEnabled();
            if (enableConstraintMatch && !constraintMatchEnabled) {
                throw new IllegalStateException("When constraintMatchEnabled (" + constraintMatchEnabled
                        + ") is disabled, this method should not be called.");
            }
            scoreDirector.setWorkingSolution(solution); // Init the ScoreDirector first, else NPEs may be thrown.
            if (triggerVariableListeners) {
                innerTriggerVariableListeners(scoreDirector, solution);
            }
            return function.apply(scoreDirector);
        }
    }
}
