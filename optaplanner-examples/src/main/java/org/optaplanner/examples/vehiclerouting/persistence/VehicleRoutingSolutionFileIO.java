package org.optaplanner.examples.vehiclerouting.persistence;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation;
import org.optaplanner.persistence.jackson.impl.domain.solution.JacksonSolutionFileIO;

public class VehicleRoutingSolutionFileIO extends JacksonSolutionFileIO<VehicleRoutingSolution> {

    public VehicleRoutingSolutionFileIO() {
        super(VehicleRoutingSolution.class);
    }

    @Override
    public VehicleRoutingSolution read(File inputSolutionFile) {
        VehicleRoutingSolution vehicleRoutingSolution = super.read(inputSolutionFile);

        var roadLocationList = vehicleRoutingSolution.getLocationList().stream()
                .filter(location -> location instanceof RoadLocation)
                .map(location -> (RoadLocation) location)
                .collect(Collectors.toList());
        var locationsById = roadLocationList.stream()
                .collect(Collectors.toMap(RoadLocation::getId, Function.identity()));
        /*
         * Replace the duplicate RoadLocation instances in the travelDistanceMap by references to instances from
         * the locationList.
         */
        for (RoadLocation roadLocation : roadLocationList) {
            var originalMap = roadLocation.getTravelDistanceMap();
            if (originalMap.isEmpty()) {
                continue;
            }
            var newMap = new LinkedHashMap<RoadLocation, Double>(originalMap.size());
            originalMap.forEach(
                    (originalLocation, distance) -> newMap.put(locationsById.get(originalLocation.getId()), distance));
            roadLocation.setTravelDistanceMap(newMap);
        }

        // Customers and depots have locations as well.
        if (!roadLocationList.isEmpty()) {
            vehicleRoutingSolution.getCustomerList()
                    .forEach(customer -> customer.setLocation(locationsById.get(customer.getLocation().getId())));
            vehicleRoutingSolution.getDepotList()
                    .forEach(depot -> depot.setLocation(locationsById.get(depot.getLocation().getId())));
        }
        return vehicleRoutingSolution;
    }

}
