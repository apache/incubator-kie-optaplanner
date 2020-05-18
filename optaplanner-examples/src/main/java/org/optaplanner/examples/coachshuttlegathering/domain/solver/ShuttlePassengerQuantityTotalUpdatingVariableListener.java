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
import org.optaplanner.examples.coachshuttlegathering.domain.Shuttle;
import org.optaplanner.examples.coachshuttlegathering.domain.StopOrHub;

public final class ShuttlePassengerQuantityTotalUpdatingVariableListener
        implements VariableListener<BusStop> {

    private void increasePassengerQuantityTotal(ScoreDirector scoreDirector, BusStop busStop) {
        if (!(busStop.getBus() instanceof Shuttle) || busStop.getPassengerQuantity() == 0) {
            return;
        }
        Bus bus = busStop.getBus();
        scoreDirector.beforeVariableChanged(bus, "passengerQuantityTotal");
        bus.setPassengerQuantityTotal(bus.getPassengerQuantityTotal() + busStop.getPassengerQuantity());
        scoreDirector.afterVariableChanged(bus, "passengerQuantityTotal");
        StopOrHub destination = bus.getDestination();
        if (destination instanceof BusStop) {
            Bus destinationBus = ((BusStop) destination).getBus();
            if (destinationBus instanceof Coach) {
                scoreDirector.beforeVariableChanged(destinationBus, "passengerQuantityTotal");
                destinationBus
                        .setPassengerQuantityTotal(destinationBus.getPassengerQuantityTotal() + busStop.getPassengerQuantity());
                scoreDirector.afterVariableChanged(destinationBus, "passengerQuantityTotal");
            }
        }
    }

    private void decreasePassengerQuantityTotal(ScoreDirector scoreDirector, BusStop busStop) {
        if (!(busStop.getBus() instanceof Shuttle) || busStop.getPassengerQuantity() == 0) {
            return;
        }
        Bus bus = busStop.getBus();
        scoreDirector.beforeVariableChanged(bus, "passengerQuantityTotal");
        bus.setPassengerQuantityTotal(bus.getPassengerQuantityTotal() - busStop.getPassengerQuantity());
        scoreDirector.afterVariableChanged(bus, "passengerQuantityTotal");
        StopOrHub destination = bus.getDestination();
        if (destination instanceof BusStop) {
            Bus destinationBus = ((BusStop) destination).getBus();
            if (destinationBus instanceof Coach) {
                scoreDirector.beforeVariableChanged(destinationBus, "passengerQuantityTotal");
                destinationBus
                        .setPassengerQuantityTotal(destinationBus.getPassengerQuantityTotal() - busStop.getPassengerQuantity());
                scoreDirector.afterVariableChanged(destinationBus, "passengerQuantityTotal");
            }
        }
    }

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, BusStop busStop) {
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, BusStop busStop) {
        increasePassengerQuantityTotal(scoreDirector, busStop);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, BusStop busStop) {
        decreasePassengerQuantityTotal(scoreDirector, busStop);
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, BusStop busStop) {
        increasePassengerQuantityTotal(scoreDirector, busStop);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, BusStop busStop) {
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, BusStop busStop) {
        decreasePassengerQuantityTotal(scoreDirector, busStop);
    }

}
