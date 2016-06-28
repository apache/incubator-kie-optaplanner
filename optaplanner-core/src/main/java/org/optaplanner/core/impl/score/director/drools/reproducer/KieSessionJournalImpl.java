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
package org.optaplanner.core.impl.score.director.drools.reproducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.Fact;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.NullFact;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.ValueFact;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionDelete;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionFireAllRules;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionOperation;
import org.optaplanner.core.impl.score.director.drools.reproducer.operation.KieSessionUpdate;

class KieSessionJournalImpl implements KieSessionJournal {

    private final List<Fact> facts;
    private final HashMap<Object, Fact> existingInstances = new HashMap<Object, Fact>();
    private final List<KieSessionInsert> initialInsertJournal;
    private final List<KieSessionOperation> updateJournal;
    private int operationId = 0;

    public KieSessionJournalImpl() {
        facts = new ArrayList<Fact>();
        initialInsertJournal = new ArrayList<KieSessionInsert>();
        updateJournal = new ArrayList<KieSessionOperation>();
    }

    public KieSessionJournalImpl(List<Fact> facts, List<KieSessionInsert> initialInsertJournal, List<KieSessionOperation> updateJournal) {
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
        for (Fact fact : facts) {
            fact.reset();
        }

        // insert facts into KIE session
        for (KieSessionOperation insert : initialInsertJournal) {
            insert.invoke(newKieSession);
        }

        // replay tested journal
        try {
            for (KieSessionOperation op : updateJournal) {
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
            Fact f = new ValueFact(i++, fact);
            facts.add(f);
            existingInstances.put(fact, f);
        }

        for (Fact fact : facts) {
            fact.setUp(existingInstances);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // KIE session operations recording
    //------------------------------------------------------------------------------------------------------------------
    //
    @Override
    public void insertInitial(Object fact) {
        initialInsertJournal.add(new KieSessionInsert(operationId++, existingInstances.get(fact)));
    }

    @Override
    public void insert(Object fact) {
        updateJournal.add(new KieSessionInsert(operationId++, existingInstances.get(fact)));
    }

    @Override
    public void update(Object entity, VariableDescriptor<?> variableDescriptor) {
        Fact entityFact = existingInstances.get(entity);
        Object value = variableDescriptor.getValue(entity);
        Fact valueFact = value == null ? new NullFact() : existingInstances.get(value);
        updateJournal.add(new KieSessionUpdate(operationId++, entityFact, variableDescriptor, valueFact));
    }

    @Override
    public void delete(Object entity) {
        updateJournal.add(new KieSessionDelete(operationId++, existingInstances.get(entity)));
    }

    @Override
    public void fireAllRules() {
        updateJournal.add(new KieSessionFireAllRules(operationId++));
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
    public List<Fact> getFacts() {
        return facts;
    }

    @Override
    public List<KieSessionInsert> getInitialInserts() {
        return initialInsertJournal;
    }

    @Override
    public List<KieSessionOperation> getMoveOperations() {
        return updateJournal;
    }

}
