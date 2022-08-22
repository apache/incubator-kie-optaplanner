package org.optaplanner.examples.investment.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.investment.domain.InvestmentSolution;

class InvestmentPerformanceTest extends SolverPerformanceTest<InvestmentSolution, HardSoftLongScore> {

    private static final String UNSOLVED_DATA_FILE = "data/investment/unsolved/irrinki_1.xml";

    @Override
    protected InvestmentApp createCommonApp() {
        return new InvestmentApp();
    }

    @Override
    protected Stream<TestData<HardSoftLongScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardSoftLongScore.ofSoft(74765),
                        HardSoftLongScore.ofSoft(74660)),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardSoftLongScore.ofSoft(74805),
                        HardSoftLongScore.ofSoft(74790)));
    }
}
