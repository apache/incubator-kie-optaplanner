package org.optaplanner.examples.pas.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.pas.domain.PatientAdmissionSchedule;

class PatientAdmissionSchedulePerformanceTest
        extends SolverPerformanceTest<PatientAdmissionSchedule, HardMediumSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/pas/unsolved/testdata01.xml";

    @Override
    protected PatientAdmissionScheduleApp createCommonApp() {
        return new PatientAdmissionScheduleApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.ofSoft(-7346),
                        HardMediumSoftScore.ofSoft(-7354)),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.ofSoft(-7312),
                        HardMediumSoftScore.ofSoft(-7354)));
    }
}
