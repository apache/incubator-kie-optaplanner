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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.drools.model.DSL;
import org.drools.model.DeclarationSource;
import org.drools.model.Variable;

public abstract class DroolsRuleStructure {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final long id = COUNTER.getAndIncrement();

    public <X> Variable<X> createVariable(Class<X> clz, String name) {
        return DSL.declarationOf(clz, "$" + name + "_" + id);
    }

    public <X> Variable<X> createVariable(Class<X> clz, String name, DeclarationSource source) {
        return DSL.declarationOf(clz, "$" + name + "_" + id, source);
    }

    public <X> Variable<X> createVariable(String name) {
        return (Variable<X>) createVariable(Object.class, name);
    }

    public <X> Variable<X> createVariable(String name, DeclarationSource source) {
        return (Variable<X>) createVariable(Object.class, name, source);
    }

}
