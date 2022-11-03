package org.optaplanner.examples.vehiclerouting.persistence;

import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.persistence.jackson.impl.domain.solution.JacksonSolutionFileIO;

public class VehicleRoutingSolutionFileIO extends JacksonSolutionFileIO<VehicleRoutingSolution> {

    public VehicleRoutingSolutionFileIO() {
        super(VehicleRoutingSolution.class);
    }
}
