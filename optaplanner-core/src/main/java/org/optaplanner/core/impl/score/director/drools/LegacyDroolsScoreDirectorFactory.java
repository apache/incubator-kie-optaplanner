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

package org.optaplanner.core.impl.score.director.drools;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

public class LegacyDroolsScoreDirectorFactory<Solution_> extends DroolsScoreDirectorFactory<Solution_> {

    protected final KieBase kieBase;

    public LegacyDroolsScoreDirectorFactory(KieBase kieBase) {
        super(kieBase);
        this.kieBase = kieBase;
        checkIfGlobalScoreHolderExists(kieBase);
    }

    public KieBase getKieBase() {
        return kieBase;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public DroolsScoreDirector<Solution_> buildScoreDirector(boolean constraintMatchEnabledPreference) {
        return new DroolsScoreDirector<>(this, constraintMatchEnabledPreference);
    }

    @Override
    public KieSession newKieSession() {
        return kieBase.newKieSession();
    }

}
