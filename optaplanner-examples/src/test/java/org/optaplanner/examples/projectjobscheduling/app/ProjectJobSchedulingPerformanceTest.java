package org.optaplanner.examples.projectjobscheduling.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;

class ProjectJobSchedulingPerformanceTest extends SolverPerformanceTest<Schedule, HardMediumSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/projectjobscheduling/unsolved/A-4.xml";

    @Override
    protected ProjectJobSchedulingApp createCommonApp() {
        return new ProjectJobSchedulingApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.of(0, -258, -103),
                        HardMediumSoftScore.of(0, -258, -114)),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.of(0, -114, -61),
                        HardMediumSoftScore.of(0, -141, -66)));
    }
}
