package org.drools.planner.benchmark.statistic;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.SimpleScore;
import org.drools.planner.core.score.HardAndSoftScore;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * @author Geoffrey De Smet
 */
public class BestScoreStatistic implements SolverStatistic {

    private List<String> configNameList = new ArrayList<String>();
    // key is the configName
    private Map<String, BestScoreStatisticListener> bestScoreStatisticListenerMap
            = new HashMap<String, BestScoreStatisticListener>();

    public void addListener(Solver solver, String configName) {
        if (configNameList.contains(configName)) {
            throw new IllegalArgumentException("Cannot add a listener with the same configName (" + configName
                    + ") twice.");
        }
        configNameList.add(configName);
        BestScoreStatisticListener bestScoreStatisticListener = new BestScoreStatisticListener();
        solver.addEventListener(bestScoreStatisticListener);
        bestScoreStatisticListenerMap.put(configName, bestScoreStatisticListener);
    }

    public void removeListener(Solver solver, String configName) {
        BestScoreStatisticListener bestScoreStatisticListener = bestScoreStatisticListenerMap.get(configName);
        solver.removeEventListener(bestScoreStatisticListener);
    }

    public void writeStatistic(File solverStatisticFilesDirectory, String baseName) {
        writeCsvStatistic(solverStatisticFilesDirectory, baseName);
        writeGraphStatistic(solverStatisticFilesDirectory, baseName);
    }

    private List<TimeToBestScoresLine> extractTimeToBestScoresLineList() {
        Map<Long, TimeToBestScoresLine> timeToBestScoresLineMap = new HashMap<Long, TimeToBestScoresLine>();
        for (Map.Entry<String, BestScoreStatisticListener> listenerEntry : bestScoreStatisticListenerMap.entrySet()) {
            String configName = listenerEntry.getKey();
            List<BestScoreStatisticPoint> statisticPointList = listenerEntry.getValue()
                    .getBestScoreStatisticPointList();
            for (BestScoreStatisticPoint statisticPoint : statisticPointList) {
                long timeMillisSpend = statisticPoint.getTimeMillisSpend();
                TimeToBestScoresLine line = timeToBestScoresLineMap.get(timeMillisSpend);
                if (line == null) {
                    line = new TimeToBestScoresLine(timeMillisSpend);
                    timeToBestScoresLineMap.put(timeMillisSpend, line);
                }
                line.getConfigNameToScoreMap().put(configName, statisticPoint.getScore());
            }
        }
        List<TimeToBestScoresLine> timeToBestScoresLineList
                = new ArrayList<TimeToBestScoresLine>(timeToBestScoresLineMap.values());
        Collections.sort(timeToBestScoresLineList);
        return timeToBestScoresLineList;
    }

    protected class TimeToBestScoresLine implements Comparable<TimeToBestScoresLine> {

        private long timeMillisSpend;
        private Map<String, Score> configNameToScoreMap;

        public TimeToBestScoresLine(long timeMillisSpend) {
            this.timeMillisSpend = timeMillisSpend;
            configNameToScoreMap = new HashMap<String, Score>();
        }

        public long getTimeMillisSpend() {
            return timeMillisSpend;
        }

        public Map<String, Score> getConfigNameToScoreMap() {
            return configNameToScoreMap;
        }

        public int compareTo(TimeToBestScoresLine other) {
            return timeMillisSpend < other.timeMillisSpend ? -1 : (timeMillisSpend > other.timeMillisSpend ? 1 : 0);
        }

    }

    private void writeCsvStatistic(File solverStatisticFilesDirectory, String baseName) {
        List<TimeToBestScoresLine> timeToBestScoresLineList = extractTimeToBestScoresLineList();
        File csvStatisticFile = new File(solverStatisticFilesDirectory, baseName + "Statistic.csv");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(csvStatisticFile), "utf-8");
            writer.append("\"TimeMillisSpend\"");
            for (String configName : configNameList) {
                writer.append(",\"").append(configName.replaceAll("\\\"","\\\"")).append("\"");
            }
            writer.append("\n");
            for (TimeToBestScoresLine timeToBestScoresLine : timeToBestScoresLineList) {
                writer.write(Long.toString(timeToBestScoresLine.getTimeMillisSpend()));
                for (String configName : configNameList) {
                    writer.append(",");
                    Score score = timeToBestScoresLine.getConfigNameToScoreMap().get(configName);
                    if (score != null) {
                        Integer scoreAlias = extractScoreAlias(score);
                        if (scoreAlias != null) {
                            writer.append(scoreAlias.toString());
                        }
                    }
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing csvStatisticFile: " + csvStatisticFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private void writeGraphStatistic(File solverStatisticFilesDirectory, String baseName) {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        for (Map.Entry<String, BestScoreStatisticListener> listenerEntry : bestScoreStatisticListenerMap.entrySet()) {
            String configName = listenerEntry.getKey();
            XYSeries configSeries = new XYSeries(configName);
            List<BestScoreStatisticPoint> statisticPointList = listenerEntry.getValue()
                    .getBestScoreStatisticPointList();
            for (BestScoreStatisticPoint statisticPoint : statisticPointList) {
                long timeMillisSpend = statisticPoint.getTimeMillisSpend();
                Score score = statisticPoint.getScore();
                Integer scoreAlias = extractScoreAlias(score);
                if (scoreAlias != null) {
                    configSeries.add(timeMillisSpend, scoreAlias);
                }
            }
            seriesCollection.addSeries(configSeries);
        }
        NumberAxis xAxis = new NumberAxis("Time millis spend");
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis("Score");
        yAxis.setAutoRangeIncludesZero(false);
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYPlot plot = new XYPlot(seriesCollection, xAxis, yAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart(baseName + " best score statistic",
                JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        BufferedImage chartImage = chart.createBufferedImage(800, 600);
        File graphStatisticFile = new File(solverStatisticFilesDirectory, baseName + "Statistic.png");
        OutputStream out = null;
        try {
            out = new FileOutputStream(graphStatisticFile);
            ImageIO.write(chartImage, "png", out);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing graphStatisticFile: " + graphStatisticFile, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private Integer extractScoreAlias(Score score) {
        // TODO Plugging in other Score implementations instead of SimpleScore and HardAndSoftScore should be possible
        Integer scoreAlias;
        if (score instanceof SimpleScore) {
            SimpleScore simpleScore = (SimpleScore) score;
            scoreAlias = simpleScore.getScore();
        } else if (score instanceof HardAndSoftScore) {
            HardAndSoftScore hardAndSoftScore = (HardAndSoftScore) score;
            if (hardAndSoftScore.getHardScore() == 0) {
                scoreAlias = hardAndSoftScore.getSoftScore();
            } else {
                scoreAlias = null;
            }
        } else {
            throw new IllegalStateException("Score class (" + score.getClass() + ") not supported.");
        }
        return scoreAlias;
    }

}
