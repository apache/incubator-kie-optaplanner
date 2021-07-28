/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.benchmark.impl.statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.config.solver.metric.SolverMetric;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListener;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.scope.SolverScope;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class StatisticRegistry extends SimpleMeterRegistry implements PhaseLifecycleListener, SolverEventListener {

    List<Consumer<Long>> stepMeterListenerList = new ArrayList<>();
    List<Consumer<Long>> bestSolutionMeterListenerList = new ArrayList<>();
    long bestSolutionChangedTimestamp = Long.MIN_VALUE;
    ScoreDefinition scoreDefinition;

    public StatisticRegistry(DefaultSolver solver) {
        scoreDefinition = solver.getSolverScope().getScoreDefinition();
    }

    public void addListener(SolverMetric metric, Consumer<Long> listener) {
        if (isMetricBestSolutionBased(metric)) {
            bestSolutionMeterListenerList.add(listener);
        } else {
            stepMeterListenerList.add(listener);
        }
    }

    private boolean isMetricBestSolutionBased(SolverMetric metric) {
        switch (metric) {
            case BEST_SCORE:
            case BEST_SOLUTION_MUTATION:
            case CONSTRAINT_MATCH_TOTAL_BEST_SCORE:
            case PICKED_MOVE_TYPE_BEST_SCORE_DIFF:
                return true;
            case MEMORY_USE:
            case STEP_SCORE:
            case ERROR_COUNT:
            case SOLVE_LENGTH:
            case MOVE_COUNT_PER_STEP:
            case SCORE_CALCULATION_SPEED:
            case PICKED_MOVE_TYPE_STEP_SCORE_DIFF:
            case CONSTRAINT_MATCH_TOTAL_STEP_SCORE:
                return false;
            default:
                throw new IllegalStateException("SolverMetric (" + metric + ") does not have a case");
        }
    }

    public Set<Meter.Id> getMeterIds(SolverMetric metric, Tags runId) {
        return Search.in(this).name(name -> name.startsWith(metric.getMeterId())).tags(runId)
                .meters().stream().map(meter -> meter.getId())
                .collect(Collectors.toSet());
    }

    public void extractScoreFromMeters(SolverMetric metric, Tags runId, Consumer<Score> scoreConsumer) {
        String[] labelNames = scoreDefinition.getLevelLabels();
        Number[] levelNumbers = new Number[labelNames.length];
        for (int i = 0; i < labelNames.length; i++) {
            Gauge scoreLevelGauge = this.find(metric.getMeterId() + "." + labelNames[i]).tags(runId).gauge();
            if (scoreLevelGauge != null) {
                levelNumbers[i] = scoreLevelGauge.value();
            } else {
                return;
            }
        }
        scoreConsumer.accept(scoreDefinition.fromLevelNumbers(0, levelNumbers));
    }

    public void getGaugeValue(SolverMetric metric, Tags runId, Consumer<Number> gaugeConsumer) {
        getGaugeValue(metric.getMeterId(), runId, gaugeConsumer);
    }

    public void getGaugeValue(String meterId, Tags runId, Consumer<Number> gaugeConsumer) {
        Gauge gauge = this.find(meterId).tags(runId).gauge();
        if (gauge != null) {
            gaugeConsumer.accept(gauge.value());
        }
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    @Override
    public void bestSolutionChanged(BestSolutionChangedEvent event) {
        if (bestSolutionChangedTimestamp != Long.MIN_VALUE) {
            bestSolutionMeterListenerList.forEach(listener -> listener.accept(bestSolutionChangedTimestamp));
        }
        bestSolutionChangedTimestamp = event.getTimeMillisSpent();
    }

    @Override
    public void stepEnded(AbstractStepScope stepScope) {
        final long timestamp =
                System.currentTimeMillis() - stepScope.getPhaseScope().getSolverScope().getStartingSystemTimeMillis();
        stepMeterListenerList.forEach(listener -> listener.accept(timestamp));
    }

    @Override
    public void phaseStarted(AbstractPhaseScope phaseScope) {
        // intentional empty
    }

    @Override
    public void stepStarted(AbstractStepScope stepScope) {
        // intentional empty
    }

    @Override
    public void phaseEnded(AbstractPhaseScope phaseScope) {
        // intentional empty
    }

    @Override
    public void solvingStarted(SolverScope solverScope) {
        // intentional empty
    }

    @Override
    public void solvingEnded(SolverScope solverScope) {
        // intentional empty
    }
}
