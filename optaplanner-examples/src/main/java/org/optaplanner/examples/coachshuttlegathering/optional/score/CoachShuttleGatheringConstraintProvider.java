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

package org.optaplanner.examples.coachshuttlegathering.optional.score;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.examples.coachshuttlegathering.domain.Bus;
import org.optaplanner.examples.coachshuttlegathering.domain.BusStop;
import org.optaplanner.examples.coachshuttlegathering.domain.Coach;
import org.optaplanner.examples.coachshuttlegathering.domain.Shuttle;
import org.optaplanner.examples.coachshuttlegathering.domain.StopOrHub;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.count;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sum;
import static org.optaplanner.core.api.score.stream.Joiners.equal;

public class CoachShuttleGatheringConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                coachStopLimit(constraintFactory),
                shuttleCapacity(constraintFactory),
//                coachCapacity(constraintFactory),
                transportTime(constraintFactory),
                shuttleDestinationIsCoachOrHub(constraintFactory),
                shuttleSetupCost(constraintFactory),
                distanceFromPrevious(constraintFactory),
                distanceBusStopToBusDestination(constraintFactory),
                distanceCoachDirectlyToDestination(constraintFactory)
        };
    }

    // ############################################################################
    // Hard constraints
    // ############################################################################

    private Constraint coachStopLimit(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BusStop.class)
                .filter(busStop -> busStop.getBus() instanceof Coach)
                .groupBy(busStop -> (Coach) busStop.getBus(), count())
                .filter((coach, stopCount) -> stopCount > coach.getStopLimit())
                .penalizeLong("coachStopLimit", HardSoftLongScore.ofUninitialized(0, 1000000, 0),
                        (coach, stopCount) -> stopCount - coach.getStopLimit());
    }

    private Constraint shuttleCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BusStop.class)
                .filter(busStop -> busStop.getBus() instanceof Shuttle)
                .groupBy(BusStop::getBus, sum(BusStop::getPassengerQuantity))
                .filter((shuttle, requiredCapacity) -> requiredCapacity > shuttle.getCapacity())
                .penalizeLong("shuttleCapacity", HardSoftLongScore.ofUninitialized(0, 1000, 0),
                        (shuttle, requiredCapacity) -> requiredCapacity - shuttle.getCapacity());
    }

    // TODO: Implement this CS
//    private Constraint coachCapacity(ConstraintFactory constraintFactory) {
//        BiConstraintStream<Coach, Integer> coachPassengerQuantity = constraintFactory.from(BusStop.class)
//                .filter(busStop -> busStop.getBus() instanceof Coach)
//                .groupBy(busStop -> (Coach) busStop.getBus(), sum(BusStop::getPassengerQuantity));
//    }

    private Constraint transportTime(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BusStop.class)
                .filter(busStop -> busStop.getTransportTimeToHub() != null && busStop.getTransportTimeRemainder() < 0)
                .penalizeLong("transportTime", HardSoftLongScore.ONE_HARD, busStop -> -1 * busStop.getTransportTimeRemainder());
    }

    private Constraint shuttleDestinationIsCoachOrHub(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Shuttle.class)
                .filter(shuttle -> shuttle.getDestination() != null)
                .join(StopOrHub.class, equal(Shuttle::getDestination, stopOrHub -> stopOrHub))
                .filter((shuttle, stopOrHub) -> !stopOrHub.isVisitedByCoach())
                .penalize("shuttleDestinationIsCoachOrHub", HardSoftLongScore.ofUninitialized(0, 1000000000, 0));
    }

    // ############################################################################
    // Soft constraints
    // ############################################################################

    private Constraint shuttleSetupCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Bus.class)
                .filter(bus -> bus.getNextStop() != null)
                .penalizeLong("shuttleSetupCost", HardSoftLongScore.ONE_SOFT, Bus::getSetupCost);
    }

    private Constraint distanceFromPrevious(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BusStop.class)
                .filter(busStop -> busStop.getPreviousBusOrStop() != null)
                .penalizeLong("distanceFromPrevious", HardSoftLongScore.ONE_SOFT, BusStop::getDistanceFromPreviousCost);
    }

    private Constraint distanceBusStopToBusDestination(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Bus.class)
                .filter(bus -> bus.getDestination() != null && bus.getNextStop() != null)
                .join(BusStop.class, equal(bus -> bus, BusStop::getBus))
                .filter((bus, busStop) -> busStop.getNextStop() == null)
                .penalizeLong("distanceBusStopToBusDestination", HardSoftLongScore.ONE_SOFT,
                          (bus, busStop) -> busStop.getDistanceToDestinationCost(bus.getDestination()));
    }

    private Constraint distanceCoachDirectlyToDestination(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Coach.class)
                .filter(coach -> coach.getDestination() != null && coach.getNextStop() == null)
                .penalizeLong("distanceCoachDirectlyToDestination", HardSoftLongScore.ONE_SOFT,
                        Coach::getDistanceToDestinationCost);
    }
}
