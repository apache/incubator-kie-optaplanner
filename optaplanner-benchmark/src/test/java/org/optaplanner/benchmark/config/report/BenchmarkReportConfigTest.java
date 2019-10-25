package org.optaplanner.benchmark.config.report;

import java.util.Locale;

import org.junit.Test;
import org.optaplanner.benchmark.config.ranking.SolverRankingType;
import org.optaplanner.benchmark.impl.ranking.TotalRankSolverRankingWeightFactory;
import org.optaplanner.benchmark.impl.ranking.TotalScoreSolverRankingComparator;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class BenchmarkReportConfigTest {

    @Test
    public void inheritBenchmarkReportConfig_WhenCallingConstructorWithConfigParameter() {
        BenchmarkReportConfig inheritance = new BenchmarkReportConfig();
        inheritance.setLocale(Locale.CANADA);
        inheritance.setSolverRankingType(SolverRankingType.TOTAL_RANKING);
        inheritance.setSolverRankingComparatorClass(TotalScoreSolverRankingComparator.class);
        inheritance.setSolverRankingWeightFactoryClass(TotalRankSolverRankingWeightFactory.class);

        BenchmarkReportConfig inheritor = new BenchmarkReportConfig(inheritance);

        assertThat(inheritor.getLocale()).isEqualTo(inheritance.getLocale());
        assertThat(inheritor.getSolverRankingType()).isEqualTo(inheritance.getSolverRankingType());
        assertThat(inheritor.getSolverRankingComparatorClass()).isEqualTo(inheritance.getSolverRankingComparatorClass());
        assertThat(inheritor.getSolverRankingWeightFactoryClass()).isEqualTo(inheritance.getSolverRankingWeightFactoryClass());
    }

    @Test
    public void throwIllegalStateException_WhenConfigContainsSolverRankingTypeAndSolverRankingComparatorClass() {
        BenchmarkReportConfig config = new BenchmarkReportConfig();
        config.setSolverRankingType(SolverRankingType.TOTAL_RANKING);
        config.setSolverRankingComparatorClass(TotalScoreSolverRankingComparator.class);

        PlannerBenchmarkResult result = mock(PlannerBenchmarkResult.class);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> config.buildBenchmarkReport(result))
                .withMessageStartingWith("The PlannerBenchmark cannot have a solverRankingType (")
                .withMessageContaining(") and a solverRankingComparatorClass (")
                .withMessageEndingWith(") at the same time.");
    }

    @Test
    public void throwIllegalStateException_WhenConfigContainsSolverRankingTypeAndSolverRankingWeightFactoryClass() {
        BenchmarkReportConfig config = new BenchmarkReportConfig();
        config.setSolverRankingType(SolverRankingType.TOTAL_RANKING);
        config.setSolverRankingWeightFactoryClass(TotalRankSolverRankingWeightFactory.class);

        PlannerBenchmarkResult result = mock(PlannerBenchmarkResult.class);

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> config.buildBenchmarkReport(result))
                .withMessageStartingWith("The PlannerBenchmark cannot have a solverRankingType (")
                .withMessageContaining(") and a solverRankingWeightFactoryClass (")
                .withMessageEndingWith(") at the same time.");
    }

    @Test
    public void throwIllegalStateException_WhenConfigContainsSolverRankingComparatorClassAndSolverRankingWeightFactoryClass() {
        BenchmarkReportConfig config = new BenchmarkReportConfig();
        config.setSolverRankingComparatorClass(TotalScoreSolverRankingComparator.class);
        config.setSolverRankingWeightFactoryClass(TotalRankSolverRankingWeightFactory.class);

        PlannerBenchmarkResult result = mock(PlannerBenchmarkResult.class);

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> config.buildBenchmarkReport(result))
                .withMessageStartingWith("The PlannerBenchmark cannot have a solverRankingComparatorClass (")
                .withMessageContaining(") and a solverRankingWeightFactoryClass (")
                .withMessageEndingWith(") at the same time.");
    }
}
