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

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.coachshuttlegathering.domain.Bus;
import org.optaplanner.examples.coachshuttlegathering.domain.BusStop;
import org.optaplanner.examples.coachshuttlegathering.domain.Coach;
import org.optaplanner.examples.coachshuttlegathering.domain.CoachShuttleGatheringSolution;
import org.optaplanner.examples.coachshuttlegathering.domain.Shuttle;
import org.optaplanner.examples.coachshuttlegathering.domain.StopOrHub;

public class ShuttlePassengerCountTotalUpdatingVariableListener implements VariableListener<Object> {

    private static void adjustBus(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, Bus bus, int difference) {
        if (difference == 0) {
            return;
        }
        scoreDirector.beforeVariableChanged(bus, "passengerQuantityTotal");
        bus.setPassengerQuantityTotal(bus.getPassengerQuantityTotal() + difference);
        scoreDirector.afterVariableChanged(bus, "passengerQuantityTotal");
    }

    private static void adjustBusStop(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop,
            boolean increase) {
        Bus bus = busStop.getBus();
        if (!(bus instanceof Shuttle)) {
            return;
        }
        System.out.println("ADJB");
        adjustBus(scoreDirector, bus, increase ? busStop.getPassengerQuantity() : -busStop.getPassengerQuantity());
        Shuttle shuttle = (Shuttle) bus;
        StopOrHub destination = shuttle.getDestination();
        if (destination instanceof BusStop) {
            Bus destinationBus = ((BusStop) destination).getBus();
            if (destinationBus instanceof Coach) {
                int difference = increase ? busStop.getPassengerQuantity() : -busStop.getPassengerQuantity();
                adjustBus(scoreDirector, destinationBus, difference);
            }
        }
    }

    private static void adjustShuttle(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, Shuttle shuttle,
            boolean increase) {
        Bus destinationBus = shuttle.getDestinationBus();
        if (destinationBus instanceof Coach) {
            adjustBus(scoreDirector, destinationBus,
                    increase ? shuttle.getPassengerQuantityTotal() : -shuttle.getPassengerQuantityTotal());
        }
    }

    private static void increase(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop) {
        adjustBusStop(scoreDirector, busStop, true);
    }

    private static void decrease(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, BusStop busStop) {
        adjustBusStop(scoreDirector, busStop, false);
    }

    private static void increase(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, Shuttle shuttle) {
        adjustShuttle(scoreDirector, shuttle, true);
    }

    private static void decrease(ScoreDirector<CoachShuttleGatheringSolution> scoreDirector, Shuttle shuttle) {
        adjustShuttle(scoreDirector, shuttle, false);
    }

    private void printSolution(String prefix, ScoreDirector<CoachShuttleGatheringSolution> scoreDirector) {
        CoachShuttleGatheringSolution solution = scoreDirector.getWorkingSolution();
        SortedMap<String, Integer> values = new TreeMap<>(solution.getBusList().stream()
                .collect(Collectors.toMap(Bus::toString, Bus::getPassengerQuantityTotal)));
        System.out.println(prefix + " " + values);
        int sum = values.values().stream()
                .mapToInt(s -> s)
                .sum();
        System.out.println(sum);
    }

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Object entity) {
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Object entity) {
        printSolution("* PRE " + entity, scoreDirector);
        if (entity instanceof BusStop) {
            increase(scoreDirector, (BusStop) entity);
        } else if (entity instanceof Shuttle) {
            increase(scoreDirector, (Shuttle) entity);
        }
        printSolution("*POST ", scoreDirector);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Object entity) {
        printSolution("- PRE " + entity, scoreDirector);
        if (Objects.equals(entity.toString(), "S2")) {
            Shuttle shuttle = (Shuttle) entity;
            System.out.println(
                    shuttle + " no longer goes to " + shuttle.getDestination() + " (" + shuttle.getDestinationBus() + ")");
        }
        if (entity instanceof BusStop) {
            decrease(scoreDirector, (BusStop) entity);
        } else if (entity instanceof Shuttle) {
            decrease(scoreDirector, (Shuttle) entity);
        }
        printSolution("-POST " + entity, scoreDirector);
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Object entity) {
        printSolution("+ PRE " + entity, scoreDirector);
        if (Objects.equals(entity.toString(), "S2")) {
            Shuttle shuttle = (Shuttle) entity;
            System.out.println(shuttle + " goes to " + shuttle.getDestination() + " (" + shuttle.getDestinationBus() + ")");
        }
        if (entity instanceof BusStop) {
            increase(scoreDirector, (BusStop) entity);
        } else if (entity instanceof Shuttle) {
            increase(scoreDirector, (Shuttle) entity);
        }
        printSolution("+POST " + entity, scoreDirector);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Object entity) {
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Object entity) {
        printSolution("X PRE " + entity, scoreDirector);
        if (entity instanceof BusStop) {
            decrease(scoreDirector, (BusStop) entity);
        } else if (entity instanceof Shuttle) {
            decrease(scoreDirector, (Shuttle) entity);
        }
        printSolution("XPOST " + entity, scoreDirector);
    }
}
