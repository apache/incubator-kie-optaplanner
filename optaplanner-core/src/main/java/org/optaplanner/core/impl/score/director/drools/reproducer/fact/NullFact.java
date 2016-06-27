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
package org.optaplanner.core.impl.score.director.drools.reproducer.fact;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class NullFact implements Fact {

    @Override
    public void setUp(Map<Object, Fact> existingInstances) {
    }

    @Override
    public List<Fact> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public List<Class<?>> getImports() {
        return Collections.emptyList();
    }

    @Override
    public void reset() {
    }

    @Override
    public void printInitialization(Logger log) {
    }

    @Override
    public void printSetup(Logger log) {
    }

    @Override
    public Object getInstance() {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

}
