package org.optaplanner.examples.cheaptime.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.cheaptime.domain.CheapTimeSolution;
import org.optaplanner.examples.common.app.SolverPerformanceTest;

class CheapTimePerformanceTest extends SolverPerformanceTest<CheapTimeSolution, HardMediumSoftLongScore> {

    private static final String UNSOLVED_DATA_FILE = "data/cheaptime/unsolved/instance00.xml";

    @Override
    protected CheapTimeApp createCommonApp() {
        return new CheapTimeApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftLongScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardMediumSoftLongScore.of(0, -1043600344878178L, -24077), EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardMediumSoftLongScore.of(0, -1047922570736971L, -23863), EnvironmentMode.FAST_ASSERT),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftLongScore.of(0, -795361927055260L, -22410), EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftLongScore.of(0, -847093214964348L, -24397), EnvironmentMode.FAST_ASSERT));
    }
}
