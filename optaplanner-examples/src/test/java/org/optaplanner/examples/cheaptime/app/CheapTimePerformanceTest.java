package org.optaplanner.examples.cheaptime.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
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
                        HardMediumSoftLongScore.of(0, -1047516879736562L, -23632),
                        HardMediumSoftLongScore.of(0, -1048339724599097L, -23580)),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftLongScore.of(0, -985796392472644L, -23719),
                        HardMediumSoftLongScore.of(0, -1046919284188901L, -23940)));
    }
}
