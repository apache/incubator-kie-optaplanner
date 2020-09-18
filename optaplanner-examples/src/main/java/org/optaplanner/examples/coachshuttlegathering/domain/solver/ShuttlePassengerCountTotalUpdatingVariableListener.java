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

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.examples.coachshuttlegathering.domain.Bus;
import org.optaplanner.examples.coachshuttlegathering.domain.BusStop;
import org.optaplanner.examples.coachshuttlegathering.domain.CoachShuttleGatheringSolution;
import org.optaplanner.examples.coachshuttlegathering.domain.Shuttle;

public class ShuttlePassengerCountTotalUpdatingVariableListener implements VariableListener<Object> {

    private static void adjustBus(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, Bus bus, int difference) {
        if (difference == 0) {
            return;
        }
        scoreDirector.beforeVariableChanged(bus, "passengerQuantityTotal");
        bus.setPassengerQuantityTotal(bus.getPassengerQuantityTotal() + difference);
        scoreDirector.afterVariableChanged(bus, "passengerQuantityTotal");
        if (bus.getPassengerQuantityTotal() < 0) {
            throw new IllegalStateException("Passenger quantity in " + bus + " got under zero here.");
        }
    }

    private static void adjustBusStop(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop,
            boolean increase) {
        Bus bus = busStop.getBus();
        if (!(bus instanceof Shuttle)) {
            return;
        }
        adjustBus(scoreDirector, bus, increase ? busStop.getPassengerQuantity() : -busStop.getPassengerQuantity());
    }

    private static void increase(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop) {
        adjustBusStop(scoreDirector, busStop, true);
    }

    private static void decrease(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop) {
        adjustBusStop(scoreDirector, busStop, false);
    }

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Object entity) {
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Object entity) {
        if (entity instanceof BusStop) {
            increase(scoreDirector, (BusStop) entity);
        }
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Object entity) {
        if (entity instanceof BusStop) {
            decrease(scoreDirector, (BusStop) entity);
        }
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Object entity) {
        if (entity instanceof BusStop) {
            increase(scoreDirector, (BusStop) entity);
        }
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Object entity) {
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Object entity) {
        if (entity instanceof BusStop) {
            decrease(scoreDirector, (BusStop) entity);
        }
    }
}
