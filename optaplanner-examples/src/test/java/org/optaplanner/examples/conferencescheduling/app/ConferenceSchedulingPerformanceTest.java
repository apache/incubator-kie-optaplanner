
package org.optaplanner.examples.conferencescheduling.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;

class ConferenceSchedulingPerformanceTest extends SolverPerformanceTest<ConferenceSolution, HardMediumSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/conferencescheduling/unsolved/72talks-12timeslots-10rooms.xlsx";

    @Override
    protected ConferenceSchedulingApp createCommonApp() {
        return new ConferenceSchedulingApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.ofSoft(-959595),
                        HardMediumSoftScore.ofSoft(-1040415)),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.ofSoft(-941580),
                        HardMediumSoftScore.ofSoft(-959595)));
    }
}
