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
import org.slf4j.Logger;

public class KieSessionFireAllRules implements KieSessionOperation {

    private final int id;

    public KieSessionFireAllRules(int id) {
        this.id = id;
    }

    @Override
    public void invoke(KieSession kieSession) {
        kieSession.fireAllRules();
    }

    @Override
    public void print(Logger log) {
        log.debug("\n        //{}", this);
        log.info("        kieSession.fireAllRules();\n");
    }

    @Override
    public String toString() {
        return "operation #" + id;
    }

}
