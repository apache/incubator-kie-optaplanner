package org.optaplanner.core.api.solver;

import java.util.UUID;

import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.ShadowVariable;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.solver.DefaultSolutionManager;

/**
 * A stateless service to help calculate {@link Score}, {@link ConstraintMatchTotal},
 * {@link Indictment}, etc.
 * <p>
 * To create a {@link SolutionManager} instance, use {@link #create(SolverFactory)}.
 * <p>
 * These methods are thread-safe unless explicitly stated otherwise.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Score_> the actual score type
 */
public interface SolutionManager<Solution_, Score_ extends Score<Score_>> {

    // ************************************************************************
    // Static creation methods: SolverFactory
    // ************************************************************************

    /**
     * Uses a {@link SolverFactory} to build a {@link SolutionManager}.
     *
     * @param solverFactory never null
     * @return never null
     * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
     * @param <Score_> the actual score type
     */
    static <Solution_, Score_ extends Score<Score_>> SolutionManager<Solution_, Score_> create(
            SolverFactory<Solution_> solverFactory) {
        return new DefaultSolutionManager<>(solverFactory);
    }

    /**
     * Uses a {@link SolverManager} to build a {@link SolutionManager}.
     *
     * @param solverManager never null
     * @return never null
     * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
     * @param <Score_> the actual score type
     * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}
     */
    static <Solution_, Score_ extends Score<Score_>, ProblemId_> SolutionManager<Solution_, Score_> create(
            SolverManager<Solution_, ProblemId_> solverManager) {
        return new DefaultSolutionManager<>(solverManager);
    }

    // ************************************************************************
    // Interface methods
    // ************************************************************************

    /**
     * As defined by {@link #updateScore(Object, boolean)},
     * without updating shadow variables.
     */
    default Score_ updateScore(Solution_ solution) {
        return updateScore(solution, false);
    }

    /**
     * Calculates the {@link Score} of a {@link PlanningSolution} and updates its {@link PlanningScore} member.
     * Optionally refreshes all the {@link ShadowVariable shadow variables},
     * incurring a performance cost.
     *
     * @param solution never null
     * @param updateShadowVariables if true, all entities get all of their shadow variables updated
     * @return never null
     */
    Score_ updateScore(Solution_ solution, boolean updateShadowVariables);

    /**
     * As defined by {@link #getSummary(Object, boolean)},
     * without updating shadow variables.
     */
    default String getSummary(Solution_ solution) {
        return getSummary(solution, false);
    }

    /**
     * Returns a diagnostic text that explains the solution through the {@link ConstraintMatch} API to identify which
     * constraints or planning entities cause that score quality.
     * Optionally refreshes all the {@link ShadowVariable shadow variables},
     * incurring a performance cost.
     * <p>
     * In case of an {@link Score#isFeasible() infeasible} solution, this can help diagnose the cause of that.
     *
     * @apiNote Do not parse the return value, its format may change without warning.
     *          Instead, to provide this information in a UI or a service, use {@link #explainScore(Object)}
     *          to retrieve {@link ScoreExplanation#getConstraintMatchTotalMap()} and
     *          {@link ScoreExplanation#getIndictmentMap()}
     *          and convert those into a domain specific API.
     * @param solution never null
     * @param updateShadowVariables if true, all entities get all of their shadow variables updated
     * @return null if {@link #updateScore(Object)} returns null with the same solution
     * @throws IllegalStateException when constraint matching is disabled or not supported by the underlying score
     *         calculator, such as {@link EasyScoreCalculator}.
     */
    String getSummary(Solution_ solution, boolean updateShadowVariables);

    /**
     * As defined by {@link #explainScore(Object, boolean)},
     * without updating shadow variables.
     */
    default ScoreExplanation<Solution_, Score_> explainScore(Solution_ solution) {
        return explainScore(solution, false);
    }

    /**
     * Calculates and retrieves {@link ConstraintMatchTotal}s and {@link Indictment}s necessary for describing the
     * quality of a particular solution.
     * Optionally refreshes all the {@link ShadowVariable shadow variables},
     * incurring a performance cost.
     *
     * @param solution never null
     * @param updateShadowVariables if true, all entities get all of their shadow variables updated
     * @return never null
     * @throws IllegalStateException when constraint matching is disabled or not supported by the underlying score
     *         calculator, such as {@link EasyScoreCalculator}.
     */
    ScoreExplanation<Solution_, Score_> explainScore(Solution_ solution, boolean updateShadowVariables);

}
