package org.optaplanner.examples.vehiclerouting.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.examples.common.app.SolverSmokeTest;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;

class VehicleRoutingSmokeTest extends SolverSmokeTest<VehicleRoutingSolution, HardSoftLongScore> {

    private static final String CVRP_32_CUSTOMERS_XML = "data/vehiclerouting/unsolved/cvrp-32customers.xml";
    private static final String CVRPTW_100_CUSTOMERS_A_XML = "data/vehiclerouting/unsolved/cvrptw-100customers-A.xml";

    @Override
    protected VehicleRoutingApp createCommonApp() {
        return new VehicleRoutingApp();
    }

    @Override
    protected Stream<TestData<HardSoftLongScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, CVRP_32_CUSTOMERS_XML,
                        HardSoftLongScore.ofSoft(-744242),
                        HardSoftLongScore.ofSoft(-744242)),
                TestData.of(ConstraintStreamImplType.DROOLS, CVRPTW_100_CUSTOMERS_A_XML,
                        HardSoftLongScore.ofSoft(-1817383),
                        HardSoftLongScore.ofSoft(-1828619)),
                TestData.of(ConstraintStreamImplType.BAVET, CVRP_32_CUSTOMERS_XML,
                        HardSoftLongScore.ofSoft(-744242),
                        HardSoftLongScore.ofSoft(-744242)),
                TestData.of(ConstraintStreamImplType.BAVET, CVRPTW_100_CUSTOMERS_A_XML,
                        HardSoftLongScore.ofSoft(-1738656),
                        HardSoftLongScore.ofSoft(-1782301)));
    }
}
