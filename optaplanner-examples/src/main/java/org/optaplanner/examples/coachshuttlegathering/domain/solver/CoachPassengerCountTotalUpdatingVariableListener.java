/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.coachshuttlegathering.domain.solver;

import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.coachshuttlegathering.domain.Bus;
import org.optaplanner.examples.coachshuttlegathering.domain.BusStop;
import org.optaplanner.examples.coachshuttlegathering.domain.Coach;
import org.optaplanner.examples.coachshuttlegathering.domain.CoachShuttleGatheringSolution;

public class CoachPassengerCountTotalUpdatingVariableListener implements VariableListener<BusStop> {

    private static void adjustBus(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, Bus bus, int difference) {
        scoreDirector.beforeVariableChanged(bus, "passengerQuantityTotal");
        System.out.println(bus + " " + bus.getPassengerQuantityTotal());
        bus.setPassengerQuantityTotal(bus.getPassengerQuantityTotal() + difference);
        System.out.println("To: " + bus.getPassengerQuantityTotal());
        scoreDirector.afterVariableChanged(bus, "passengerQuantityTotal");
    }

    private static void adjust(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop,
            boolean increase) {
        Bus bus = busStop.getBus();
        if (!(bus instanceof Coach)) {
            return;
        }
        adjustBus(scoreDirector, bus, increase ? busStop.getPassengerQuantity() : -busStop.getPassengerQuantity());
    }

    private static void increase(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop) {
        adjust(scoreDirector, busStop, true);
    }

    private static void decrease(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop) {
        adjust(scoreDirector, busStop, false);
    }

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, BusStop busStop) {
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, BusStop busStop) {
        increase(scoreDirector, busStop);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, BusStop busStop) {
        decrease(scoreDirector, busStop);
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, BusStop busStop) {
        increase(scoreDirector, busStop);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, BusStop busStop) {
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, BusStop busStop) {
        decrease(scoreDirector, busStop);
    }
}
