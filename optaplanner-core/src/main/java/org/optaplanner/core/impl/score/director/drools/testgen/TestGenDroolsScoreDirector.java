/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.score.director.drools.testgen;

import java.util.Collection;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirectorFactory;

public class TestGenDroolsScoreDirector<Solution_> extends DroolsScoreDirector<Solution_> {

    private final KieSessionJournal journal = new KieSessionJournal();

    public TestGenDroolsScoreDirector(DroolsScoreDirectorFactory<Solution_> scoreDirectorFactory, boolean constraintMatchEnabledPreference) {
        super(scoreDirectorFactory, constraintMatchEnabledPreference);
    }

    @Override
    public void setWorkingSolution(Solution_ workingSolution) {
        super.setWorkingSolution(workingSolution);
        journal.dispose();
        Collection<Object> workingFacts = getWorkingFacts();
        journal.addFacts(workingFacts);
        for (Object fact : workingFacts) {
            journal.insertInitial(fact);
        }
    }

    @Override
    public Score calculateScore() {
        journal.fireAllRules();
        try {
            return super.calculateScore();
        } catch (RuntimeException e) {
            DroolsReproducer.replay(journal, new DroolsExceptionReproducer(e, kieSession));
            // This is important so that the original exception is never swallowed
            throw new IllegalStateException("Reproducer should have failed!");
        }
    }

    @Override
    protected String buildScoreCorruptionAnalysis(ScoreDirector<Solution_> uncorruptedScoreDirector) {
        String originalAnalysis = super.buildScoreCorruptionAnalysis(uncorruptedScoreDirector);
        CorruptedScoreReproducer reproducer = new CorruptedScoreReproducer(
                originalAnalysis, kieSession, getScoreDefinition(), constraintMatchEnabledPreference);
        DroolsReproducer.replay(journal, reproducer);
        return originalAnalysis;
    }

    @Override
    public Collection<ConstraintMatchTotal> getConstraintMatchTotals() {
        journal.fireAllRules();
        return super.getConstraintMatchTotals();
    }

    @Override
    public void dispose() {
        journal.dispose();
        super.dispose();
    }

    @Override
    public void afterEntityAdded(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        journal.insert(entity);
        super.afterEntityAdded(entityDescriptor, entity);
    }

    @Override
    public void afterVariableChanged(VariableDescriptor variableDescriptor, Object entity) {
        journal.update(entity, variableDescriptor);
        super.afterVariableChanged(variableDescriptor, entity);
    }

    @Override
    public void afterEntityRemoved(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        journal.delete(entity);
        super.afterEntityRemoved(entityDescriptor, entity);
    }

    @Override
    public void afterProblemFactAdded(Object problemFact) {
        journal.insert(problemFact);
        super.afterProblemFactAdded(problemFact);
    }

    // TODO override afterProblemFactChanged()?
    @Override
    public void afterProblemFactRemoved(Object problemFact) {
        journal.delete(problemFact);
        super.afterProblemFactRemoved(problemFact);
    }

}
