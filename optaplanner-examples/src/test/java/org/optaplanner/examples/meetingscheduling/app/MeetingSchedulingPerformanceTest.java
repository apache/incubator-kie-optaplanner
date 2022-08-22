package org.optaplanner.examples.meetingscheduling.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.meetingscheduling.domain.MeetingSchedule;

class MeetingSchedulingPerformanceTest extends SolverPerformanceTest<MeetingSchedule, HardMediumSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/meetingscheduling/unsolved/50meetings-160timegrains-5rooms.xlsx";

    @Override
    protected MeetingSchedulingApp createCommonApp() {
        return new MeetingSchedulingApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(-26, -480, -9058),
                        EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(-29, -344, -9227),
                        EnvironmentMode.FAST_ASSERT),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(0, 0, -5118),
                        EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(0, 0, -5126),
                        EnvironmentMode.FAST_ASSERT));
    }
}
