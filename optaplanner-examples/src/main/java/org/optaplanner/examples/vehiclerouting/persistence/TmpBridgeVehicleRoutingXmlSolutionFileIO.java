/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.vehiclerouting.persistence;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Depot;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class TmpBridgeVehicleRoutingXmlSolutionFileIO implements SolutionFileIO<VehicleRoutingSolution> {

    private final VehicleRoutingXmlSolutionFileIO origIO = new VehicleRoutingXmlSolutionFileIO();

    @Override
    public String getInputFileExtension() {
        return origIO.getInputFileExtension();
    }

    @Override
    public VehicleRoutingSolution read(File inputSolutionFile) {
        org.optaplanner.examples.vehiclerouting.domain.chained.VehicleRoutingSolution origSolution =
                origIO.read(inputSolutionFile);

        VehicleRoutingSolution solution = new VehicleRoutingSolution();
        solution.setName(origSolution.getName());
        solution.setDistanceType(origSolution.getDistanceType());
        solution.setDistanceUnitOfMeasurement(origSolution.getDistanceUnitOfMeasurement());
        solution.setScore(origSolution.getScore());

        solution.setLocationList(origSolution.getLocationList());

        Map<Long, Depot> depotByIdMap = origSolution.getDepotList().stream()
                .map(depot -> new Depot(depot.getId(), depot.getLocation()))
                .collect(toMap(AbstractPersistable::getId, Function.identity()));
        solution.setDepotList(List.copyOf(depotByIdMap.values()));

        List<Vehicle> vehicleList = origSolution.getVehicleList().stream()
                .map(vehicle -> new Vehicle(vehicle.getId(), vehicle.getCapacity(), depotByIdMap.get(vehicle.getDepot().getId())))
                .collect(toList());
        solution.setVehicleList(vehicleList);

        List<Customer> customerList = origSolution.getCustomerList().stream()
                .map(customer -> new Customer(customer.getId(), customer.getLocation(), customer.getDemand()))
                .collect(toList());
        solution.setCustomerList(customerList);

        return solution;
    }

    @Override
    public void write(VehicleRoutingSolution vehicleRoutingSolution, File outputSolutionFile) {
        throw new UnsupportedOperationException("Not yet.");
    }
}
