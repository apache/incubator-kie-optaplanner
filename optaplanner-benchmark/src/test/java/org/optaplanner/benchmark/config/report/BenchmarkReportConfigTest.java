package org.optaplanner.benchmark.config.report;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.optaplanner.benchmark.config.ranking.SolverRankingType;
import org.optaplanner.benchmark.impl.ranking.TotalRankSolverRankingWeightFactory;
import org.optaplanner.benchmark.impl.ranking.TotalScoreSolverRankingComparator;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;

import static org.mockito.Mockito.mock;

public class BenchmarkReportConfigTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void inheritBenchmarkReportConfig_WhenCallingConstructorWithConfigParameter() {
        BenchmarkReportConfig inheritance = new BenchmarkReportConfig();
        inheritance.setLocale(Locale.CANADA);
        inheritance.setSolverRankingType(SolverRankingType.TOTAL_RANKING);
        inheritance.setSolverRankingComparatorClass(TotalScoreSolverRankingComparator.class);
        inheritance.setSolverRankingWeightFactoryClass(TotalRankSolverRankingWeightFactory.class);

        BenchmarkReportConfig inheritor = new BenchmarkReportConfig(inheritance);

        Assert.assertEquals(inheritance.getLocale(), inheritor.getLocale());
        Assert.assertEquals(inheritance.getSolverRankingType(), inheritor.getSolverRankingType());
        Assert.assertEquals(inheritance.getSolverRankingComparatorClass(), inheritor.getSolverRankingComparatorClass());
        Assert.assertEquals(inheritance.getSolverRankingWeightFactoryClass(), inheritor.getSolverRankingWeightFactoryClass());
    }

    @Test
    public void throwIllegalStateException_WhenConfigContainsSolverRankingTypeAndSolverRankingComparatorClass() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("The PlannerBenchmark cannot have a solverRankingType (");
        exceptionRule.expectMessage(") and a solverRankingComparatorClass (");
        exceptionRule.expectMessage(") at the same time.");

        BenchmarkReportConfig config = new BenchmarkReportConfig();
        config.setSolverRankingType(SolverRankingType.TOTAL_RANKING);
        config.setSolverRankingComparatorClass(TotalScoreSolverRankingComparator.class);

        PlannerBenchmarkResult result = mock(PlannerBenchmarkResult.class);

        config.buildBenchmarkReport(result);
    }

    @Test
    public void throwIllegalStateException_WhenConfigContainsSolverRankingTypeAndSolverRankingWeightFactoryClass() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("The PlannerBenchmark cannot have a solverRankingType (");
        exceptionRule.expectMessage(") and a solverRankingWeightFactoryClass (");
        exceptionRule.expectMessage(") at the same time.");

        BenchmarkReportConfig config = new BenchmarkReportConfig();
        config.setSolverRankingType(SolverRankingType.TOTAL_RANKING);
        config.setSolverRankingWeightFactoryClass(TotalRankSolverRankingWeightFactory.class);

        PlannerBenchmarkResult result = mock(PlannerBenchmarkResult.class);

        config.buildBenchmarkReport(result);
    }

    @Test
    public void throwIllegalStateException_WhenConfigContainsSolverRankingComparatorClassAndSolverRankingWeightFactoryClass() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("The PlannerBenchmark cannot have a solverRankingComparatorClass (");
        exceptionRule.expectMessage(") and a solverRankingWeightFactoryClass (");
        exceptionRule.expectMessage(") at the same time.");

        BenchmarkReportConfig config = new BenchmarkReportConfig();
        config.setSolverRankingComparatorClass(TotalScoreSolverRankingComparator.class);
        config.setSolverRankingWeightFactoryClass(TotalRankSolverRankingWeightFactory.class);

        PlannerBenchmarkResult result = mock(PlannerBenchmarkResult.class);

        config.buildBenchmarkReport(result);
    }

}
