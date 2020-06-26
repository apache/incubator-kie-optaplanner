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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.score.director.ScoreDirector;

class UsageVariableListenerTest {

    private final Location location = new Location(1, 1);

    @Test
    void should_update_facility_usage() {
        int usage = 100;
        int demand = 33;
        ScoreDirector<?> scoreDirector = mock(ScoreDirector.class);
        UsageVariableListener variableListener = new UsageVariableListener();

        Facility facility = new Facility(0, location, 0, 1000);
        facility.setUsage(usage);

        DemandPoint demandPoint = new DemandPoint(0, location, demand);

        // Should not fail if the demandPoint is uninitialized.
        assertDoesNotThrow(() -> variableListener.beforeVariableChanged(scoreDirector, demandPoint));

        demandPoint.setFacility(facility);
        variableListener.afterVariableChanged(scoreDirector, demandPoint);

        assertEquals(usage + demand, facility.getUsage());

        // demandPoint is going to move to another facility.
        variableListener.beforeVariableChanged(scoreDirector, demandPoint);

        // Its current facility's usage should be reduced.
        assertEquals(usage, facility.getUsage());

        // Should not fail if the demandPoint is uninitialized.
        demandPoint.setFacility(null);
        assertDoesNotThrow(() -> variableListener.afterVariableChanged(scoreDirector, demandPoint));
    }

}
