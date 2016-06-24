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
package org.optaplanner.core.impl.score.director.drools.reproducer.operation;

import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.score.director.drools.reproducer.fact.Fact;

public class KieSessionDelete implements KieSessionOperation {

    private final int id;
    private final Fact entity;

    public KieSessionDelete(int id, Fact entity) {
        this.id = id;
        this.entity = entity;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void invoke(KieSession kieSession) {
        kieSession.delete(kieSession.getFactHandle(entity.getInstance()));
    }

    @Override
    public String toString() {
        return "        kieSession.delete(kieSession.getFactHandle(" + entity + "), " + entity + ");";
    }

}
