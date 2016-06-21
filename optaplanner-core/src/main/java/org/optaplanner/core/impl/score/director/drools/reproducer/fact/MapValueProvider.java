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

import java.util.Map;

import org.slf4j.Logger;

public class MapValueProvider extends AbstractValueProvider {

    final String identifier;
    final Map<Object, Fact> existingInstances;

    public MapValueProvider(Object value, String identifier, Map<Object, Fact> existingInstances) {
        super(value);
        this.identifier = identifier;
        this.existingInstances = existingInstances;
    }

    @Override
    public void printSetup(Logger log) {
        log.info("        java.util.HashMap {} = new java.util.HashMap();", identifier);
        for (Map.Entry<? extends Object, ? extends Object> entry : ((java.util.Map<?, ?>) value).entrySet()) {
            log.info("        //{} => {}", entry.getKey(), entry.getValue());
            log.info("        {}.put({}, {});", identifier, existingInstances.get(entry.getKey()), existingInstances.get(entry.getValue()));
        }
    }

    @Override
    public String toString() {
        return identifier;
    }

}
