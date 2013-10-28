/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.benchmark.impl.statistic.bestsolutionmutation;

import java.awt.BasicStroke;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.optaplanner.benchmark.impl.DefaultPlannerBenchmark;
import org.optaplanner.benchmark.impl.ProblemBenchmark;
import org.optaplanner.benchmark.impl.SingleBenchmark;
import org.optaplanner.benchmark.impl.statistic.AbstractProblemStatistic;
import org.optaplanner.benchmark.impl.statistic.MillisecondsSpendNumberFormat;
import org.optaplanner.benchmark.impl.statistic.ProblemStatisticType;
import org.optaplanner.benchmark.impl.statistic.SingleStatistic;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;

public class BestSolutionMutationProblemStatistic extends AbstractProblemStatistic {

    protected File graphStatisticFile = null;

    public BestSolutionMutationProblemStatistic(ProblemBenchmark problemBenchmark) {
        super(problemBenchmark, ProblemStatisticType.BEST_SOLUTION_MUTATION);
    }

    public SingleStatistic createSingleStatistic() {
        return new BestSolutionMutationSingleStatistic();
    }

    /**
     * @return never null, relative to the {@link DefaultPlannerBenchmark#benchmarkReportDirectory}
     * (not {@link ProblemBenchmark#problemReportDirectory})
     */
    public String getGraphFilePath() {
        return toFilePath(graphStatisticFile);
    }

    // ************************************************************************
    // Write methods
    // ************************************************************************

    protected void writeCsvStatistic() {
        ProblemStatisticCsv csv = new ProblemStatisticCsv();
        for (SingleBenchmark singleBenchmark : problemBenchmark.getSingleBenchmarkList()) {
            if (singleBenchmark.isSuccess()) {
                BestSolutionMutationSingleStatistic singleStatisticState = (BestSolutionMutationSingleStatistic)
                        singleBenchmark.getSingleStatistic(problemStatisticType);
                for (BestSolutionMutationSingleStatisticPoint point : singleStatisticState.getPointList()) {
                    long timeMillisSpend = point.getTimeMillisSpend();
                    csv.addPoint(singleBenchmark, timeMillisSpend, point.getMutationCount());
                }
            } else {
                csv.addPoint(singleBenchmark, 0L, "Failed");
            }
        }
        csvStatisticFile = new File(problemBenchmark.getProblemReportDirectory(),
                problemBenchmark.getName() + "BestSolutionMutationStatistic.csv");
        csv.writeCsvStatisticFile();
    }

    protected void writeGraphStatistic() {
        Locale locale = problemBenchmark.getPlannerBenchmark().getBenchmarkReport().getLocale();
        NumberAxis xAxis = new NumberAxis("Time spend");
        xAxis.setNumberFormatOverride(new MillisecondsSpendNumberFormat(locale));
        NumberAxis yAxis = new NumberAxis("Best solution mutation count");
        yAxis.setNumberFormatOverride(NumberFormat.getInstance(locale));
        yAxis.setAutoRangeIncludesZero(true);
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        int seriesIndex = 0;
        for (SingleBenchmark singleBenchmark : problemBenchmark.getSingleBenchmarkList()) {
            XYSeries series = new XYSeries(singleBenchmark.getSolverBenchmark().getNameWithFavoriteSuffix());
            XYItemRenderer renderer = new XYLineAndShapeRenderer();
            if (singleBenchmark.isSuccess()) {
                BestSolutionMutationSingleStatistic singleStatisticState = (BestSolutionMutationSingleStatistic)
                        singleBenchmark.getSingleStatistic(problemStatisticType);
                for (BestSolutionMutationSingleStatisticPoint point : singleStatisticState.getPointList()) {
                    long timeMillisSpend = point.getTimeMillisSpend();
                    long mutationCount = point.getMutationCount();
                    series.add(timeMillisSpend, mutationCount);
                }
            }
            plot.setDataset(seriesIndex, new XYSeriesCollection(series));

            if (singleBenchmark.getSolverBenchmark().isFavorite()) {
                // Make the favorite more obvious
                renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            }
            plot.setRenderer(seriesIndex, renderer);
            seriesIndex++;
        }
        JFreeChart chart = new JFreeChart(problemBenchmark.getName() + " best solution mutation statistic",
                JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        graphStatisticFile = writeChartToImageFile(chart, problemBenchmark.getName() + "BestSolutionMutationStatistic");
    }
    
    @Override
    public SingleStatistic readSingleStatistic(File file, ScoreDirectorFactoryConfig scoreConfig) {
        List<BestSolutionMutationSingleStatisticPoint> pointList = new ArrayList<BestSolutionMutationSingleStatisticPoint>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] values = line.split(",");
                long timeSpent = Long.valueOf(values[0]);
                int mutationCount = Integer.valueOf(values[1].substring(1, values[1].length() - 1));
                pointList.add(new BestSolutionMutationSingleStatisticPoint(timeSpent, mutationCount));
            }
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("Could not open statistic file " + file, ex);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Error while reading statistic file " + file, ex);
        }
        BestSolutionMutationSingleStatistic statistic = new BestSolutionMutationSingleStatistic();
        statistic.setPointList(pointList);
        return statistic;
    }

}
