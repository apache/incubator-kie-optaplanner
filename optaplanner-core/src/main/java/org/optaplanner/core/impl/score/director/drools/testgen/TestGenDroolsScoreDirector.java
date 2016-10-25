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

import java.io.File;
import java.util.Collection;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.holder.ScoreHolder;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.drools.testgen.reproducer.TestGenCorruptedScoreException;
import org.optaplanner.core.impl.score.director.drools.testgen.reproducer.TestGenCorruptedScoreReproducer;
import org.optaplanner.core.impl.score.director.drools.testgen.reproducer.TestGenDroolsExceptionReproducer;

public class TestGenDroolsScoreDirector<Solution_> extends DroolsScoreDirector<Solution_> {

    private final TestGenKieSessionJournal journal = new TestGenKieSessionJournal();
    private final File testFile = new File("DroolsReproducerTest.java");

    public TestGenDroolsScoreDirector(DroolsScoreDirectorFactory<Solution_> scoreDirectorFactory, boolean constraintMatchEnabledPreference) {
        super(scoreDirectorFactory, constraintMatchEnabledPreference);
    }

    public KieSession createKieSession() {
        KieSession newKieSession = getScoreDirectorFactory().newKieSession();

        // set a fresh score holder
        ScoreDefinition<?> scoreDefinition = getScoreDefinition();
        if (scoreDefinition != null) {
            ScoreHolder sh = scoreDefinition.buildScoreHolder(constraintMatchEnabledPreference);
            newKieSession.setGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY, sh);
        }

        return newKieSession;
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
            // catch any Drools exception and create a minimal reproducing test
            // TODO check the exception is coming from org.drools
            TestGenDroolsExceptionReproducer reproducer = new TestGenDroolsExceptionReproducer(e, this);
            TestGenKieSessionJournal minJournal = TestGenerator.minimize(journal, reproducer);
            TestGenTestWriter.print(minJournal, testFile);
            throw wrapOriginalException(e);
        }
    }

    @Override
    public void assertWorkingScoreFromScratch(Score workingScore, Object completedAction) {
        try {
            super.assertWorkingScoreFromScratch(workingScore, completedAction);
        } catch (IllegalStateException e) {
            // catch corrupted score exception and create a minimal reproducing test
            // TODO check it's really corrupted score
            TestGenCorruptedScoreReproducer reproducer = new TestGenCorruptedScoreReproducer(e.getMessage(), this);
            TestGenKieSessionJournal minJournal = TestGenerator.minimize(journal, reproducer);
            Score<?> uncorruptedScore = workingScore;
            try {
                minJournal.replay(createKieSession());
            } catch (TestGenCorruptedScoreException tgcse) {
                uncorruptedScore = tgcse.getUncorruptedScore();
            }
            TestGenTestWriter.printWithScoreAssert(
                    minJournal,
                    getScoreDefinition().getClass(),
                    constraintMatchEnabledPreference,
                    uncorruptedScore.toString(),
                    testFile);
            throw wrapOriginalException(e);
        }
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

    private RuntimeException wrapOriginalException(RuntimeException e) {
        return new RuntimeException(e.getMessage() + "\nDrools test written to: " + testFile.getAbsolutePath(), e);
    }

}
