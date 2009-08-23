package org.drools.solver.benchmark.statistic;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import org.drools.solver.core.Solver;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.SimpleScore;
import org.drools.solver.core.score.HardAndSoftScore;
import org.apache.commons.io.IOUtils;

/**
 * @author Geoffrey De Smet
 */
public class BestScoreStatistic implements SolverStatistic {

    // A LinkedHashMap because the order is important
    private Map<String, BestScoreStatisticListener> bestScoreStatisticListenerMap
            = new LinkedHashMap<String, BestScoreStatisticListener>();

    public void addListener(Solver solver) {
        addListener(solver, "solver");
    }

    public void addListener(Solver solver, String configName) {
        if (bestScoreStatisticListenerMap.containsKey(configName)) {
            throw new IllegalArgumentException("Cannot add a listener with the same configName (" + configName
                    + ") twice.");
        }
        BestScoreStatisticListener bestScoreStatisticListener = new BestScoreStatisticListener();
        solver.addEventListener(bestScoreStatisticListener);
        bestScoreStatisticListenerMap.put(configName, bestScoreStatisticListener);
    }

    public void removeListener(Solver solver) {
        removeListener(solver, "solver");
    }

    public void removeListener(Solver solver, String configName) {
        BestScoreStatisticListener bestScoreStatisticListener = bestScoreStatisticListenerMap.get(configName);
        solver.removeEventListener(bestScoreStatisticListener);
    }

    public void writeStatistic(File solverStatisticFilesDirectory, String baseName) {
        // The configNameSet is ordered because it comes from a LinkedHashMap 
        Set<String> configNameSet = bestScoreStatisticListenerMap.keySet();
        List<TimeToBestScoresLine> timeToBestScoresLineList = extractTimeToBestScoresLineList();
        File statisticFile = new File(solverStatisticFilesDirectory, baseName + "Statistic.csv");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(statisticFile), "utf-8");
            writer.append("\"TimeMillisSpend\"");
            for (String configName : configNameSet) {
                writer.append(",\"").append(configName.replaceAll("\\\"","\\\"")).append("\"");
            }
            writer.append("\n");
            for (TimeToBestScoresLine timeToBestScoresLine : timeToBestScoresLineList) {
                writer.write(Long.toString(timeToBestScoresLine.getTimeMillisSpend()));
                for (String configName : configNameSet) {
                    writer.append(",");
                    Score score = timeToBestScoresLine.getConfigNameToScoreMap().get(configName);
                    if (score != null) {
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
                        if (scoreAlias != null) {
                            writer.append(scoreAlias.toString());
                        }
                    }
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing statisticFile: " + statisticFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
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

}
