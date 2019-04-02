/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.solver.event;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.EventObject;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.impl.score.ScoreUtils;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Delivered when the solver ends, including before every {@link Solver#addProblemFactChange(ProblemFactChange) restart}.
 * Delivered in the solver thread (which is the thread that calls {@link Solver#solve}).
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SolvingEndedEvent<Solution_> extends EventObject {

    private final Solver<Solution_> solver;
    private final long timeMillisSpent;
    private final long scoreCalculationCount;

    /**
     * @param solver never null
     * @param timeMillisSpent
     * @param scoreCalculationCount at least 0
     */
    public SolvingEndedEvent(Solver<Solution_> solver, long timeMillisSpent, long scoreCalculationCount) {
        super(solver);
        this.solver = solver;
        this.timeMillisSpent = timeMillisSpent;
        this.scoreCalculationCount = scoreCalculationCount;
    }

    /**
     * Alternative for {@link #getDurationSpent()}.
     * @return {@code >= 0}, the amount of millis spent since the last {@link Solver} (re)start
     * and this ending
     * @see #getDurationSpent()
     */
    public long getTimeMillisSpent() {
        return timeMillisSpent;
    }

    /**
     * @return never null, the duration spent since the last {@link Solver} (re)start
     * and this ending
     */
    public Duration getDurationSpent() {
        return Duration.of(timeMillisSpent, ChronoUnit.MILLIS);
    }

    /**
     * The score calculation count is typically the number of moves evaluated in the solver run.
     * <p>
     * For comparisons, it's often better to use {@link #getScoreCalculationSpeed()} instead,
     * because it is a relative number.
     * @return {@code >= 0}, the number of times {@link ScoreDirector#calculateScore()} was called
     * in the {@link #getTimeMillisSpent()} time span.
     * @see #getScoreCalculationSpeed()
     */
    public long getScoreCalculationCount() {
        return scoreCalculationCount;
    }

    /**
     * The score calculation speed is typically the number of moves evaluated per second.
     * <p>
     * A short run usually has a higher speed than a longer run
     * because they spend a bigger relative proportion of their time in Construction Heuristics.
     * The speed of Construction Heuristics is sometimes twice as fast as that of Local Search,
     * due its uninitialized {@link PlanningEntity entities} and its low level moves.
     * @return at least 0, the number of times {@link ScoreDirector#calculateScore()} was called per second
     */
    public long getScoreCalculationSpeed() {
        return ScoreUtils.calculateScoreCalculationSpeed(scoreCalculationCount, timeMillisSpent);
    }

}
