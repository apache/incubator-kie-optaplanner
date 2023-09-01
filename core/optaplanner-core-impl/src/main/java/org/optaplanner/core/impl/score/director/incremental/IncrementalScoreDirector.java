/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.score.director.incremental;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.calculator.ConstraintMatchAwareIncrementalScoreCalculator;
import org.optaplanner.core.api.score.calculator.IncrementalScoreCalculator;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.constraint.DefaultIndictment;
import org.optaplanner.core.impl.score.director.AbstractScoreDirector;

/**
 * Incremental java implementation of {@link ScoreDirector}, which only recalculates the {@link Score}
 * of the part of the {@link PlanningSolution working solution} that changed,
 * instead of the going through the entire {@link PlanningSolution}. This is incremental calculation, which is fast.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Score_> the score type to go with the solution
 * @see ScoreDirector
 */
public class IncrementalScoreDirector<Solution_, Score_ extends Score<Score_>>
        extends AbstractScoreDirector<Solution_, Score_, IncrementalScoreDirectorFactory<Solution_, Score_>> {

    private final IncrementalScoreCalculator<Solution_, Score_> incrementalScoreCalculator;

    public IncrementalScoreDirector(IncrementalScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory,
            boolean lookUpEnabled, boolean constraintMatchEnabledPreference,
            IncrementalScoreCalculator<Solution_, Score_> incrementalScoreCalculator) {
        super(scoreDirectorFactory, lookUpEnabled, constraintMatchEnabledPreference);
        this.incrementalScoreCalculator = incrementalScoreCalculator;
    }

    public IncrementalScoreCalculator<Solution_, Score_> getIncrementalScoreCalculator() {
        return incrementalScoreCalculator;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public void setWorkingSolution(Solution_ workingSolution) {
        super.setWorkingSolution(workingSolution);
        if (incrementalScoreCalculator instanceof ConstraintMatchAwareIncrementalScoreCalculator) {
            ((ConstraintMatchAwareIncrementalScoreCalculator<Solution_, ?>) incrementalScoreCalculator)
                    .resetWorkingSolution(workingSolution, constraintMatchEnabledPreference);
        } else {
            incrementalScoreCalculator.resetWorkingSolution(workingSolution);
        }
    }

    @Override
    public Score_ calculateScore() {
        variableListenerSupport.assertNotificationQueuesAreEmpty();
        Score_ score = incrementalScoreCalculator.calculateScore();
        if (score == null) {
            throw new IllegalStateException("The incrementalScoreCalculator (" + incrementalScoreCalculator.getClass()
                    + ") must return a non-null score (" + score + ") in the method calculateScore().");
        } else if (!score.isSolutionInitialized()) {
            throw new IllegalStateException("The score (" + this + ")'s initScore (" + score.initScore()
                    + ") should be 0.\n"
                    + "Maybe the score calculator (" + incrementalScoreCalculator.getClass() + ") is calculating "
                    + "the initScore too, although it's the score director's responsibility.");
        }
        if (workingInitScore != 0) {
            score = score.withInitScore(workingInitScore);
        }
        setCalculatedScore(score);
        return score;
    }

    @Override
    public boolean isConstraintMatchEnabled() {
        return constraintMatchEnabledPreference
                && incrementalScoreCalculator instanceof ConstraintMatchAwareIncrementalScoreCalculator;
    }

    @Override
    public Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        if (!isConstraintMatchEnabled()) {
            throw new IllegalStateException("When constraintMatchEnabled (" + isConstraintMatchEnabled()
                    + ") is disabled in the constructor, this method should not be called.");
        }
        // Notice that we don't trigger the variable listeners
        return ((ConstraintMatchAwareIncrementalScoreCalculator<Solution_, Score_>) incrementalScoreCalculator)
                .getConstraintMatchTotals()
                .stream()
                .collect(toMap(ConstraintMatchTotal<Score_>::getConstraintId, identity()));
    }

    @Override
    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        if (!isConstraintMatchEnabled()) {
            throw new IllegalStateException("When constraintMatchEnabled (" + isConstraintMatchEnabled()
                    + ") is disabled in the constructor, this method should not be called.");
        }
        Map<Object, Indictment<Score_>> incrementalIndictmentMap =
                ((ConstraintMatchAwareIncrementalScoreCalculator<Solution_, Score_>) incrementalScoreCalculator)
                        .getIndictmentMap();
        if (incrementalIndictmentMap != null) {
            return incrementalIndictmentMap;
        }
        Map<Object, Indictment<Score_>> indictmentMap = new LinkedHashMap<>(); // TODO use entitySize
        Score_ zeroScore = getScoreDefinition().getZeroScore();
        Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotalMap = getConstraintMatchTotalMap();
        for (ConstraintMatchTotal<Score_> constraintMatchTotal : constraintMatchTotalMap.values()) {
            for (ConstraintMatch<Score_> constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
                constraintMatch.getIndictedObjectList()
                        .stream()
                        .distinct() // One match might have the same indictment twice.
                        .forEach(fact -> {
                            DefaultIndictment<Score_> indictment =
                                    (DefaultIndictment<Score_>) indictmentMap.computeIfAbsent(fact,
                                            k -> new DefaultIndictment<>(fact, zeroScore));
                            indictment.addConstraintMatch(constraintMatch);
                        });
            }
        }
        return indictmentMap;
    }

    @Override
    public boolean requiresFlushing() {
        return true; // Incremental may decide to keep events for delayed processing.
    }

    // ************************************************************************
    // Entity/variable add/change/remove methods
    // ************************************************************************

    @Override
    public void beforeEntityAdded(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        incrementalScoreCalculator.beforeEntityAdded(entity);
        super.beforeEntityAdded(entityDescriptor, entity);
    }

    @Override
    public void afterEntityAdded(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        incrementalScoreCalculator.afterEntityAdded(entity);
        super.afterEntityAdded(entityDescriptor, entity);
    }

    @Override
    public void beforeVariableChanged(VariableDescriptor variableDescriptor, Object entity) {
        incrementalScoreCalculator.beforeVariableChanged(entity, variableDescriptor.getVariableName());
        super.beforeVariableChanged(variableDescriptor, entity);
    }

    @Override
    public void afterVariableChanged(VariableDescriptor variableDescriptor, Object entity) {
        incrementalScoreCalculator.afterVariableChanged(entity, variableDescriptor.getVariableName());
        super.afterVariableChanged(variableDescriptor, entity);
    }

    // TODO Add support for list variable (https://issues.redhat.com/browse/PLANNER-2711).

    @Override
    public void beforeListVariableElementAssigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        incrementalScoreCalculator.beforeListVariableElementAssigned(variableDescriptor.getVariableName(), element);
        super.beforeListVariableElementAssigned(variableDescriptor, element);
    }

    @Override
    public void afterListVariableElementAssigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        incrementalScoreCalculator.afterListVariableElementAssigned(variableDescriptor.getVariableName(), element);
        super.afterListVariableElementAssigned(variableDescriptor, element);
    }

    @Override
    public void beforeListVariableElementUnassigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        incrementalScoreCalculator.beforeListVariableElementUnassigned(variableDescriptor.getVariableName(), element);
        super.beforeListVariableElementUnassigned(variableDescriptor, element);
    }

    @Override
    public void afterListVariableElementUnassigned(ListVariableDescriptor<Solution_> variableDescriptor, Object element) {
        incrementalScoreCalculator.afterListVariableElementUnassigned(variableDescriptor.getVariableName(), element);
        super.afterListVariableElementUnassigned(variableDescriptor, element);
    }

    @Override
    public void beforeListVariableChanged(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex) {
        incrementalScoreCalculator.beforeListVariableChanged(entity, variableDescriptor.getVariableName(), fromIndex, toIndex);
        super.beforeListVariableChanged(variableDescriptor, entity, fromIndex, toIndex);
    }

    @Override
    public void afterListVariableChanged(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex) {
        incrementalScoreCalculator.afterListVariableChanged(entity, variableDescriptor.getVariableName(), fromIndex, toIndex);
        super.afterListVariableChanged(variableDescriptor, entity, fromIndex, toIndex);
    }

    @Override
    public void beforeEntityRemoved(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        incrementalScoreCalculator.beforeEntityRemoved(entity);
        super.beforeEntityRemoved(entityDescriptor, entity);
    }

    @Override
    public void afterEntityRemoved(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        incrementalScoreCalculator.afterEntityRemoved(entity);
        super.afterEntityRemoved(entityDescriptor, entity);
    }

    // ************************************************************************
    // Problem fact add/change/remove methods
    // ************************************************************************

    @Override
    public void beforeProblemFactAdded(Object problemFact) {
        super.beforeProblemFactAdded(problemFact);
    }

    @Override
    public void afterProblemFactAdded(Object problemFact) {
        incrementalScoreCalculator.resetWorkingSolution(workingSolution); // TODO do not nuke it
        super.afterProblemFactAdded(problemFact);
    }

    @Override
    public void beforeProblemPropertyChanged(Object problemFactOrEntity) {
        super.beforeProblemPropertyChanged(problemFactOrEntity);
    }

    @Override
    public void afterProblemPropertyChanged(Object problemFactOrEntity) {
        incrementalScoreCalculator.resetWorkingSolution(workingSolution); // TODO do not nuke it
        super.afterProblemPropertyChanged(problemFactOrEntity);
    }

    @Override
    public void beforeProblemFactRemoved(Object problemFact) {
        super.beforeProblemFactRemoved(problemFact);
    }

    @Override
    public void afterProblemFactRemoved(Object problemFact) {
        incrementalScoreCalculator.resetWorkingSolution(workingSolution); // TODO do not nuke it
        super.afterProblemFactRemoved(problemFact);
    }

}
