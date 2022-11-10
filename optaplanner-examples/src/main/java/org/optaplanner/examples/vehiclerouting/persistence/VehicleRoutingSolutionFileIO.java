package org.optaplanner.examples.vehiclerouting.persistence;

import java.io.File;
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
        /*
         * Replace the duplicate MrMachine instances in the machineMoveCostMap by references to instances from
         * the machineList.
         */
        deduplicateEntities(vehicleRoutingSolution, solution -> vehicleRoutingSolution.getLocationList()
                .stream()
                .filter(location -> location instanceof RoadLocation)
                .map(location -> (RoadLocation) location)
                .collect(Collectors.toList()),
                RoadLocation::getId, RoadLocation::getTravelDistanceMap, RoadLocation::setTravelDistanceMap);
        return vehicleRoutingSolution;
    }

}
