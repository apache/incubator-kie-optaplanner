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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.testgen.fact.TestGenFact;
import org.optaplanner.core.impl.score.director.drools.testgen.fact.TestGenNullFact;
import org.optaplanner.core.impl.score.director.drools.testgen.fact.TestGenValueFact;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionDelete;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionFireAllRules;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionUpdate;

class KieSessionJournalImpl implements KieSessionJournal {

    private final List<TestGenFact> facts;
    private final HashMap<Object, TestGenFact> existingInstances = new HashMap<Object, TestGenFact>();
    private final List<TestGenKieSessionInsert> initialInsertJournal;
    private final List<TestGenKieSessionOperation> updateJournal;
    private int operationId = 0;

    public KieSessionJournalImpl() {
        facts = new ArrayList<TestGenFact>();
        initialInsertJournal = new ArrayList<TestGenKieSessionInsert>();
        updateJournal = new ArrayList<TestGenKieSessionOperation>();
    }

    public KieSessionJournalImpl(List<TestGenFact> facts, List<TestGenKieSessionInsert> initialInsertJournal, List<TestGenKieSessionOperation> updateJournal) {
        this.facts = facts;
        this.initialInsertJournal = initialInsertJournal;
        this.updateJournal = updateJournal;
    }

    @Override
    public RuntimeException replay(KieSession kieSession) {
        KieSession newKieSession = kieSession.getKieBase().newKieSession();

        for (String globalKey : kieSession.getGlobals().getGlobalKeys()) {
            newKieSession.setGlobal(globalKey, kieSession.getGlobal(globalKey));
        }

        // reset facts to the original state
        for (TestGenFact fact : facts) {
            fact.reset();
        }

        // insert facts into KIE session
        for (TestGenKieSessionOperation insert : initialInsertJournal) {
            insert.invoke(newKieSession);
        }

        // replay tested journal
        try {
            for (TestGenKieSessionOperation op : updateJournal) {
                op.invoke(newKieSession);
            }
            return null;
        } catch (RuntimeException ex) {
            return ex;
        } finally {
            newKieSession.dispose();
        }
    }

    @Override
    public void addFacts(Collection<Object> workingFacts) {
        int i = 0;
        for (Object fact : workingFacts) {
            TestGenFact f = new TestGenValueFact(i++, fact);
            facts.add(f);
            existingInstances.put(fact, f);
        }

        for (TestGenFact fact : facts) {
            fact.setUp(existingInstances);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // KIE session operations recording
    //------------------------------------------------------------------------------------------------------------------
    //
    @Override
    public void insertInitial(Object fact) {
        initialInsertJournal.add(new TestGenKieSessionInsert(operationId++, existingInstances.get(fact)));
    }

    @Override
    public void insert(Object fact) {
        updateJournal.add(new TestGenKieSessionInsert(operationId++, existingInstances.get(fact)));
    }

    @Override
    public void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        TestGenFact entityFact = existingInstances.get(entity);
        Object value = variableDescriptor.getValue(entity);
        TestGenFact valueFact = value == null ? new TestGenNullFact() : existingInstances.get(value);
        updateJournal.add(new TestGenKieSessionUpdate(operationId++, entityFact, variableDescriptor, valueFact));
    }

    @Override
    public void delete(Object entity) {
        updateJournal.add(new TestGenKieSessionDelete(operationId++, existingInstances.get(entity)));
    }

    @Override
    public void fireAllRules() {
        updateJournal.add(new TestGenKieSessionFireAllRules(operationId++));
    }

    @Override
    public void dispose() {
        facts.clear();
        existingInstances.clear();
        initialInsertJournal.clear();
        updateJournal.clear();
        operationId = 0;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Getters
    //------------------------------------------------------------------------------------------------------------------
    //
    @Override
    public List<TestGenFact> getFacts() {
        return facts;
    }

    @Override
    public List<TestGenKieSessionInsert> getInitialInserts() {
        return initialInsertJournal;
    }

    @Override
    public List<TestGenKieSessionOperation> getMoveOperations() {
        return updateJournal;
    }

}
