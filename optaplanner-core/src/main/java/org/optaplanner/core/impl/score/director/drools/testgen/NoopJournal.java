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
import java.util.Collections;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.score.director.drools.testgen.fact.TestGenFact;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionInsert;
import org.optaplanner.core.impl.score.director.drools.testgen.operation.TestGenKieSessionOperation;

class NoopJournal implements KieSessionJournal {

    @Override
    public void addFacts(Collection<Object> workingFacts) {
    }

    @Override
    public void delete(Object entity) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void fireAllRules() {
    }

    @Override
    public List<TestGenFact> getFacts() {
        return Collections.emptyList();
    }

    @Override
    public List<TestGenKieSessionInsert> getInitialInserts() {
        return Collections.emptyList();
    }

    @Override
    public List<TestGenKieSessionOperation> getMoveOperations() {
        return Collections.emptyList();
    }

    @Override
    public void insert(Object fact) {
    }

    @Override
    public void insertInitial(Object fact) {
    }

    @Override
    public void update(Object entity, VariableDescriptor<?> variableDescriptor) {
    }

    @Override
    public RuntimeException replay(KieSession kieSession) {
        return null;
    }

}
