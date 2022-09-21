package org.optaplanner.examples.vehiclerouting.optional.benchmark;

import org.optaplanner.examples.common.app.CommonBenchmarkApp;

public class VehicleRoutingBenchmarkApp extends CommonBenchmarkApp {

    public static void main(String[] args) {
        new VehicleRoutingBenchmarkApp().buildAndBenchmark(args);
    }

    public VehicleRoutingBenchmarkApp() {
        super(
                new ArgOption("default",
                        "org/optaplanner/examples/vehiclerouting/optional/benchmark/vehicleRoutingBenchmarkConfig.xml"),
                new ArgOption("stepLimit",
                        "org/optaplanner/examples/vehiclerouting/optional/benchmark/vehicleRoutingStepLimitBenchmarkConfig.xml"),
                new ArgOption("scoreDirector",
                        "org/optaplanner/examples/vehiclerouting/optional/benchmark/vehicleRoutingScoreDirectorBenchmarkConfig.xml")
        // FIXME Nearby selection for a list variable not yet supported: https://issues.redhat.com/browse/PLANNER-2814
        // new ArgOption("template",
        //         "org/optaplanner/examples/vehiclerouting/optional/benchmark/vehicleRoutingBenchmarkConfigTemplate.xml.ftl",
        //         true)
        );
    }

}
