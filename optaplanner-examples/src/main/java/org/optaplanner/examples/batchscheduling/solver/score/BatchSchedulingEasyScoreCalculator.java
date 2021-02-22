package org.optaplanner.examples.batchscheduling.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import org.optaplanner.examples.batchscheduling.app.BatchSchedulingApp;
import org.optaplanner.examples.batchscheduling.domain.Allocation;
import org.optaplanner.examples.batchscheduling.domain.AllocationPath;
import org.optaplanner.examples.batchscheduling.domain.RoutePath;
import org.optaplanner.examples.batchscheduling.domain.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchSchedulingEasyScoreCalculator implements EasyScoreCalculator<Schedule, BendableLongScore> {

    final Logger logger = LoggerFactory.getLogger(BatchSchedulingEasyScoreCalculator.class);

    // Refer to IncrementalScoreCalculator for comments
    public BendableLongScore calculateScore(Schedule schedule) {

        long hard0Score = 0;
        long hard1Score = 0;
        long hard2Score = 0;
        long soft0Score = 0;
        long soft1Score = 0;

        Map<Long, String> batchRoutePathMap = new HashMap<Long, String>();
        Map<Long, Long> batchOtherPenaltyValueMap = new HashMap<Long, Long>();
        Map<Long, Long> batchCurrentPenaltyValueMap = new HashMap<Long, Long>();
        Map<String, Long> segmentOverlapMap = new HashMap<String, Long>();

        for (AllocationPath allocationPath : schedule.getAllocationPathList()) {

            batchOtherPenaltyValueMap.put(allocationPath.getBatch().getId(), 0L);
            batchCurrentPenaltyValueMap.put(allocationPath.getBatch().getId(), 0L);

            if (allocationPath.getRoutePath() == null) {
                continue;
            }

            batchRoutePathMap.put(allocationPath.getBatch().getId(), allocationPath.getRoutePath().getPath());

        }

        for (Allocation allocation : schedule.getAllocationList()) {

            if (batchRoutePathMap.get(allocation.getBatch().getId()) == null) {
                batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(),
                        batchCurrentPenaltyValueMap.get(allocation.getBatch().getId()) + 1);
                continue;
            }

            if (!(allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId())))) {

                if (allocation.getDelay() != null) {
                    batchOtherPenaltyValueMap.put(allocation.getBatch().getId(),
                            batchOtherPenaltyValueMap.get(allocation.getBatch().getId()) + 1);
                }
                continue;
            }

            if (allocation.getDelay() == null) {
                batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(),
                        batchCurrentPenaltyValueMap.get(allocation.getBatch().getId()) + 1);
                continue;
            }

            Long allocationStartInjectionTime = allocation.getStartInjectionTime();

            if (allocationStartInjectionTime == null) {
                continue;
            }

            Long allocationEndInjectionTime = allocation.getEndInjectionTime();
            Long allocationStartDeliveryTime = allocation.getStartDeliveryTime();
            Long allocationEndDeliveryTime = allocation.getEndDeliveryTime();

            for (Allocation allocation2 : schedule.getAllocationList()) {

                if (allocation2.getDelay() == null) {
                    continue;
                }

                if (allocation2.getBatch().getId() == allocation.getBatch().getId()) {
                    continue;
                }

                if (batchRoutePathMap.get(allocation2.getBatch().getId()) == null) {
                    continue;
                }

                if (!(allocation2.getRoutePath().getPath()
                        .equals(batchRoutePathMap.get(allocation2.getBatch().getId())))) {
                    continue;
                }

                if (!(allocation2.getSegment().getName().equals(allocation.getSegment().getName()))) {
                    continue;
                }

                Long allocation2StartInjectionTime = allocation2.getStartInjectionTime();

                if (allocation2StartInjectionTime == null) {
                    continue;
                }

                Long allocation2EndInjectionTime = allocation2.getEndInjectionTime();
                Long allocation2StartDeliveryTime = allocation2.getStartDeliveryTime();
                Long allocation2EndDeliveryTime = allocation2.getEndDeliveryTime();

                Long newOverlapPenaltyValue = 0L;

                if ((allocation2StartInjectionTime <= allocation.getStartInjectionTime())
                        && (allocation2EndInjectionTime >= allocationEndInjectionTime)) {
                    newOverlapPenaltyValue = allocationEndInjectionTime - allocation.getStartInjectionTime();
                } else if ((allocation2StartInjectionTime >= allocation.getStartInjectionTime())
                        && (allocation2EndInjectionTime <= allocationEndInjectionTime)) {
                    newOverlapPenaltyValue = allocation2EndInjectionTime - allocation2StartInjectionTime;
                } else if ((allocation2StartInjectionTime <= allocation.getStartInjectionTime())
                        && (allocation2EndInjectionTime > allocation.getStartInjectionTime())) {
                    newOverlapPenaltyValue = allocation2EndInjectionTime - allocation.getStartInjectionTime();
                } else if ((allocation2StartInjectionTime < allocationEndInjectionTime)
                        && (allocation2EndInjectionTime >= allocationEndInjectionTime)) {
                    newOverlapPenaltyValue = allocationEndInjectionTime - allocation2StartInjectionTime;
                }

                if ((allocation2StartDeliveryTime <= allocationStartDeliveryTime)
                        && (allocation2EndDeliveryTime >= allocationEndDeliveryTime)) {
                    newOverlapPenaltyValue += allocationEndDeliveryTime - allocationStartDeliveryTime;
                } else if ((allocation2StartDeliveryTime >= allocationStartDeliveryTime)
                        && (allocation2EndDeliveryTime <= allocationEndDeliveryTime)) {
                    newOverlapPenaltyValue += allocation2EndDeliveryTime - allocation2StartDeliveryTime;
                } else if ((allocation2StartDeliveryTime <= allocationStartDeliveryTime)
                        && (allocation2EndDeliveryTime > allocationStartDeliveryTime)) {
                    newOverlapPenaltyValue += allocation2EndDeliveryTime - allocationStartDeliveryTime;
                } else if ((allocation2StartDeliveryTime < allocationEndDeliveryTime)
                        && (allocation2EndDeliveryTime >= allocationEndDeliveryTime)) {
                    newOverlapPenaltyValue += allocationEndDeliveryTime - allocation2StartDeliveryTime;
                }

                if ((allocation2StartInjectionTime >= allocation.getStartInjectionTime())
                        && (allocation2EndDeliveryTime <= allocationEndDeliveryTime)) {
                    newOverlapPenaltyValue += allocationEndDeliveryTime - allocation2EndDeliveryTime;
                }

                if ((allocation2StartInjectionTime <= allocation.getStartInjectionTime())
                        && (allocation2EndDeliveryTime >= allocationEndDeliveryTime)) {
                    newOverlapPenaltyValue += allocation2EndDeliveryTime - allocationEndDeliveryTime;
                }

                if (newOverlapPenaltyValue > 0L) {
                    segmentOverlapMap.put(allocation.getSegment().getId().toString() + "#"
                            + allocation2.getSegment().getId().toString(), newOverlapPenaltyValue);
                }
            }
        }

        for (Map.Entry<Long, Long> entry : batchCurrentPenaltyValueMap.entrySet()) {

            if ((entry.getValue() > 0) || (batchRoutePathMap.get(entry.getKey()) == null)) {
                hard0Score -= BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
                hard1Score -= entry.getValue();
            }
        }

        for (Map.Entry<Long, Long> entry : batchOtherPenaltyValueMap.entrySet()) {

            if (batchRoutePathMap.get(entry.getKey()) == null) {
                continue;
            }

            if (entry.getValue() > 0) {
                hard0Score -= BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
                hard1Score -= entry.getValue();
            }
        }

        for (Map.Entry<String, Long> entry : segmentOverlapMap.entrySet()) {
            hard2Score -= entry.getValue();
        }

        for (Allocation allocation : schedule.getAllocationList()) {
            if (batchRoutePathMap.get(allocation.getBatch().getId()) == null) {
                continue;
            }

            if (allocation.getEndDeliveryTime() == null) {
                continue;
            }

            if (!(allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId())))) {
                continue;
            }

            long longEndDeliverytime = allocation.getEndDeliveryTime();

            if (longEndDeliverytime <= soft0Score) {
                continue;
            }

            soft0Score = longEndDeliverytime;
        }

        long segmentCount = 0L;

        Map<String, Boolean> segmentMapCount = new HashMap<String, Boolean>();

        for (Allocation allocation : schedule.getAllocationList()) {
            segmentMapCount.put(allocation.getSegment().getName(), true);
        }

        segmentCount = segmentMapCount.size();

        Map<String, Boolean> segmentMap = new HashMap<String, Boolean>();

        for (AllocationPath allocationPath : schedule.getAllocationPathList()) {
            if (allocationPath.getRoutePath() == null) {
                continue;
            }

            for (String s : RoutePath.getSegmentArray(allocationPath.getRoutePath().getPath())) {
                segmentMap.put(s, true);
            }
        }

        soft0Score = -soft0Score;
        soft1Score = segmentMap.size() - segmentCount;

        logger.debug(String.format("%-" + 50 + "." + 50 + "s", "Basic") + ":: " + hard0Score + " " + hard1Score + " "
                + hard2Score + " / " + soft0Score + " " + soft1Score);

        return BendableLongScore.of(new long[] { hard0Score, hard1Score, hard2Score },
                new long[] { soft0Score, soft1Score });
    }

}
