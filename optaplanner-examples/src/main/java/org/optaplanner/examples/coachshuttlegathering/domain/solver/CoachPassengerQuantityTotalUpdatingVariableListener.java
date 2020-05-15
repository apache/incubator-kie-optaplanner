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
import org.optaplanner.examples.coachshuttlegathering.domain.BusStop;
import org.optaplanner.examples.coachshuttlegathering.domain.Coach;

public class CoachPassengerQuantityTotalUpdatingVariableListener implements VariableListener<BusStop> {

    private void increaseCoachPassengerQuantityTotal(ScoreDirector scoreDirector, BusStop busStop) {
        if (busStop.getPassengerQuantity() == 0) {
            return;
        }
        if (!(busStop.getBus() instanceof Coach)) {
            return;
        }
        Coach coach = (Coach) busStop.getBus();
        int originalPassengerQuantityTotal = coach.getPassengerQuantityTotal() == null ?
                0 : coach.getPassengerQuantityTotal();

        scoreDirector.beforeVariableChanged(coach, "passengerQuantityTotal");
        coach.setPassengerQuantityTotal(originalPassengerQuantityTotal + busStop.getPassengerQuantity());
        scoreDirector.afterVariableChanged(coach, "passengerQuantityTotal");
    }

    private void decreaseCoachPassengerQuantityTotal(ScoreDirector scoreDirector, BusStop busStop) {
        if (busStop.getPassengerQuantity() == 0) {
            return;
        }
        if (!(busStop.getBus() instanceof Coach)) {
            return;
        }
        Coach coach = (Coach) busStop.getBus();
        scoreDirector.beforeVariableChanged(coach, "passengerQuantityTotal");
        coach.setPassengerQuantityTotal(coach.getPassengerQuantityTotal() - busStop.getPassengerQuantity());
        scoreDirector.afterVariableChanged(coach, "passengerQuantityTotal");
    }

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, BusStop busStop) {
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, BusStop busStop) {
        increaseCoachPassengerQuantityTotal(scoreDirector, busStop);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, BusStop busStop) {
        decreaseCoachPassengerQuantityTotal(scoreDirector, busStop);
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, BusStop busStop) {
        increaseCoachPassengerQuantityTotal(scoreDirector, busStop);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, BusStop busStop) {
        decreaseCoachPassengerQuantityTotal(scoreDirector, busStop);
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, BusStop busStop) {
    }
}
