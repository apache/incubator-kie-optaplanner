/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.facilitylocation.domain;

import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class UsageVariableListener implements VariableListener<DemandPoint> {

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, DemandPoint demandPoint) {
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, DemandPoint demandPoint) {
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, DemandPoint demandPoint) {
        Facility oldFacility = demandPoint.getFacility();
        if (oldFacility == null) {
            return;
        }
        scoreDirector.beforeVariableChanged(oldFacility, "usage");
        oldFacility.remove(demandPoint);
        scoreDirector.afterVariableChanged(oldFacility, "usage");
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, DemandPoint demandPoint) {
        Facility newFacility = demandPoint.getFacility();
        if (newFacility == null) {
            return;
        }
        scoreDirector.beforeVariableChanged(newFacility, "usage");
        newFacility.add(demandPoint);
        scoreDirector.afterVariableChanged(newFacility, "usage");
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, DemandPoint demandPoint) {
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, DemandPoint demandPoint) {
    }
}
