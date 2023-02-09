package org.optaplanner.core.api.solver;

import static org.optaplanner.core.api.solver.SolutionUpdatePolicy.ALL;
import static org.optaplanner.core.api.solver.SolutionUpdatePolicy.SCORE_ONLY;

import java.util.UUID;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
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
     * As defined by {@link #update(Object, SolutionUpdatePolicy)},
     * using {@link SolutionUpdatePolicy#ALL}.
     *
     */
    default Score_ update(Solution_ solution) {
        return update(solution, ALL);
    }

    /**
     * Updates the given solution according to the {@link SolutionUpdatePolicy}.
     *
     * @param solution never null
     * @param solutionUpdatePolicy never null; if unsure, pick {@link SolutionUpdatePolicy#ALL}
     * @return possibly null if already null and {@link SolutionUpdatePolicy} didn't cause its update
     * @see SolutionUpdatePolicy Description of individual policies with respect to performance trade-offs.
     */
    Score_ update(Solution_ solution, SolutionUpdatePolicy solutionUpdatePolicy);

    /**
     * Returns a diagnostic text that explains the solution through the {@link ConstraintMatch} API to identify which
     * constraints or planning entities cause that score quality.
     * <p>
     * In case of an {@link Score#isFeasible() infeasible} solution, this can help diagnose the cause of that.
     *
     * @apiNote Do not parse the return value, its format may change without warning.
     *          Instead, to provide this information in a UI or a service, use {@link #explain(Object)}
     *          to retrieve {@link ScoreExplanation#getConstraintMatchTotalMap()} and
     *          {@link ScoreExplanation#getIndictmentMap()}
     *          and convert those into a domain specific API.
     * @deprecated Use {@link ScoreExplanation#getSummary()} obtained by {@link #explain(Object)}.
     * @param solution never null
     * @return never null
     * @throws IllegalStateException when constraint matching is disabled or not supported by the underlying score
     *         calculator, such as {@link EasyScoreCalculator}.
     */
    @Deprecated(forRemoval = true)
    default String getSummary(Solution_ solution) {
        return explainScore(solution)
                .getSummary();
    }

    /**
     * As defined by {@link #explain(Object)},
     * using {@link SolutionUpdatePolicy#ALL}.
     */
    default ScoreExplanation<Solution_, Score_> explain(Solution_ solution) {
        return explain(solution, ALL);
    }

    /**
     * Calculates and retrieves {@link ConstraintMatchTotal}s and {@link Indictment}s necessary for describing the
     * quality of a particular solution.
     *
     * @param solution never null
     * @param solutionUpdatePolicy never null; if unsure, pick {@link SolutionUpdatePolicy#ALL}
     * @return never null
     * @throws IllegalStateException when constraint matching is disabled or not supported by the underlying score
     *         calculator, such as {@link EasyScoreCalculator}.
     * @see SolutionUpdatePolicy Description of individual policies with respect to performance trade-offs.
     */
    ScoreExplanation<Solution_, Score_> explain(Solution_ solution, SolutionUpdatePolicy solutionUpdatePolicy);

    /**
     * As defined by {@link #update(Object, SolutionUpdatePolicy)},
     * using {@link SolutionUpdatePolicy#SCORE_ONLY}.
     *
     * @deprecated Use {@link #update(Object)} as is
     *             or {@link #update(Object, SolutionUpdatePolicy)},
     *             picking the update policy that best suits your planning solution.
     */
    @Deprecated(forRemoval = true)
    default Score_ updateScore(Solution_ solution) {
        return update(solution, SCORE_ONLY);
    }

    /**
     * As defined by {@link #explain(Object, SolutionUpdatePolicy)},
     * using {@link SolutionUpdatePolicy#SCORE_ONLY}.
     *
     * @deprecated Use {@link #explain(Object)} as is
     *             or {@link #explain(Object, SolutionUpdatePolicy)},
     *             picking the update policy that best suits your planning solution.
     */
    @Deprecated(forRemoval = true)
    default ScoreExplanation<Solution_, Score_> explainScore(Solution_ solution) {
        return explain(solution, SCORE_ONLY);
    }

}
