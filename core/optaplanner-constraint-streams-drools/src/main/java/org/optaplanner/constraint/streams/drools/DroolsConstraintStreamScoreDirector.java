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

package org.optaplanner.constraint.streams.drools;

import java.util.Map;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.constraint.streams.common.inliner.AbstractScoreInliner;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirector;

/**
 * FP streams implementation of {@link ScoreDirector}, which only recalculates the {@link Score}
 * of the part of the {@link PlanningSolution working solution} that changed,
 * instead of the going through the entire {@link PlanningSolution}. This is incremental calculation, which is fast.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @see ScoreDirector
 */
public final class DroolsConstraintStreamScoreDirector<Solution_, Score_ extends Score<Score_>>
        extends AbstractScoreDirector<Solution_, Score_, DroolsConstraintStreamScoreDirectorFactory<Solution_, Score_>> {

    private final SolutionDescriptor<Solution_> solutionDescriptor;

    private KieSession session;
    private AgendaFilter agendaFilter;
    private AbstractScoreInliner<Score_> scoreInliner;

    public DroolsConstraintStreamScoreDirector(
            DroolsConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory,
            boolean lookUpEnabled, boolean constraintMatchEnabledPreference) {
        super(scoreDirectorFactory, lookUpEnabled, constraintMatchEnabledPreference);
        this.solutionDescriptor = scoreDirectorFactory.getSolutionDescriptor();
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public void setWorkingSolution(Solution_ workingSolution) {
        super.setWorkingSolution(workingSolution);
        resetConstraintStreamingSession();
    }

    private void resetConstraintStreamingSession() {
        if (session != null) {
            session.dispose();
        }
        SessionDescriptor<Score_> sessionDescriptor =
                scoreDirectorFactory.newSession(constraintMatchEnabledPreference, workingSolution);
        session = sessionDescriptor.getSession();
        agendaFilter = sessionDescriptor.getAgendaFilter();
        scoreInliner = sessionDescriptor.getScoreInliner();
        getSolutionDescriptor().visitAll(workingSolution, session::insert);
    }

    @Override
    public Score_ calculateScore() {
        variableListenerSupport.assertNotificationQueuesAreEmpty();
        session.fireAllRules(agendaFilter);
        Score_ score = scoreInliner.extractScore(workingInitScore);
        setCalculatedScore(score);
        return score;
    }

    @Override
    public boolean isConstraintMatchEnabled() {
        return constraintMatchEnabledPreference;
    }

    @Override
    public Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        if (workingSolution == null) {
            throw new IllegalStateException(
                    "The method setWorkingSolution() must be called before the method getConstraintMatchTotalMap().");
        }
        session.fireAllRules(agendaFilter);
        return scoreInliner.getConstraintMatchTotalMap();
    }

    @Override
    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        if (workingSolution == null) {
            throw new IllegalStateException(
                    "The method setWorkingSolution() must be called before the method getIndictmentMap().");
        }
        session.fireAllRules(agendaFilter);
        return scoreInliner.getIndictmentMap();
    }

    @Override
    public boolean requiresFlushing() {
        return true; // Drools propagation queue is only flushed during fireAllRules().
    }

    @Override
    public void close() {
        super.close();
        if (session != null) {
            session.dispose();
            session = null;
            agendaFilter = null;
            scoreInliner = null;
        }
    }

    // ************************************************************************
    // Entity/variable add/change/remove methods
    // ************************************************************************

    // public void beforeEntityAdded(EntityDescriptor entityDescriptor, Object entity) // Do nothing

    @Override
    public void afterEntityAdded(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The entity (" + entity + ") cannot be added to the ScoreDirector.");
        }
        if (!getSolutionDescriptor().hasEntityDescriptor(entity.getClass())) {
            throw new IllegalArgumentException("The entity (" + entity + ") of class (" + entity.getClass()
                    + ") is not a configured @" + PlanningEntity.class.getSimpleName() + ".");
        }
        session.insert(entity);
        super.afterEntityAdded(entityDescriptor, entity);
    }

    // public void beforeVariableChanged(VariableDescriptor variableDescriptor, Object entity) // Do nothing

    @Override
    public void afterVariableChanged(VariableDescriptor<Solution_> variableDescriptor, Object entity) {
        update(entity);
        super.afterVariableChanged(variableDescriptor, entity);
    }

    @Override
    public void afterListVariableChanged(ListVariableDescriptor<Solution_> variableDescriptor, Object entity, int fromIndex,
            int toIndex) {
        update(entity);
        super.afterListVariableChanged(variableDescriptor, entity, fromIndex, toIndex);
    }

    // public void beforeEntityRemoved(EntityDescriptor entityDescriptor, Object entity) // Do nothing

    @Override
    public void afterEntityRemoved(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        retract(entity);
        super.afterEntityRemoved(entityDescriptor, entity);
    }

    // ************************************************************************
    // Problem fact add/change/remove methods
    // ************************************************************************

    // public void beforeProblemFactAdded(Object problemFact) // Do nothing

    @Override
    public void afterProblemFactAdded(Object problemFact) {
        if (problemFact == null) {
            throw new IllegalArgumentException("The problemFact (" + problemFact + ") cannot be added to the ScoreDirector.");
        }
        session.insert(problemFact);
        super.afterProblemFactAdded(problemFact);
    }

    // public void beforeProblemPropertyChanged(Object problemFactOrEntity) // Do nothing

    @Override
    public void afterProblemPropertyChanged(Object problemFactOrEntity) {
        update(problemFactOrEntity);
        super.afterProblemPropertyChanged(problemFactOrEntity);
    }

    // public void beforeProblemFactRemoved(Object problemFact) // Do nothing

    @Override
    public void afterProblemFactRemoved(Object problemFact) {
        retract(problemFact);
        super.afterProblemFactRemoved(problemFact);
    }

    private void update(Object fact) {
        FactHandle factHandle = session.getFactHandle(fact);
        if (factHandle == null) {
            /*
             * Drools will eliminate all facts that do not match any rules.
             * Therefore fact handle can be null here even if the fact/entity was inserted before.
             * We could solve this by introducing a tracking map in the score director,
             * but in the interest of saving memory, we just re-insert it here.
             */
            session.insert(fact);
        } else {
            session.update(factHandle, fact);
        }
    }

    private void retract(Object fact) {
        FactHandle factHandle = session.getFactHandle(fact);
        if (factHandle != null) {
            /*
             * Drools will eliminate all facts that do not match any rules.
             * Therefore fact handle can be null here even if the fact/entity was inserted before.
             * We could solve this by introducing a tracking map in the score director,
             * but in the interest of saving memory, we just ignore null fact handles.
             */
            session.delete(factHandle);
        }
    }

}
