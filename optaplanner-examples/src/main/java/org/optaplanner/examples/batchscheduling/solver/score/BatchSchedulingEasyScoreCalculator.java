package org.optaplanner.examples.batchscheduling.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import org.optaplanner.examples.batchscheduling.domain.Allocation;
import org.optaplanner.examples.batchscheduling.domain.AllocationPath;
import org.optaplanner.examples.batchscheduling.domain.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchSchedulingEasyScoreCalculator implements EasyScoreCalculator<Schedule, BendableLongScore> {

    final Logger logger = LoggerFactory.getLogger(BatchSchedulingEasyScoreCalculator.class);
    public static final String ROUTE_PATH_SEPERATOR = "---";
    private static final int OTHER_ALLOCATION_PENALTY = 1;
    private static final int CURRENT_ALLOCATION_PENALTY = 1;
    private static final boolean COMPUTE_hard1Score = true;
    private static final boolean COMPUTE_soft2Score = true;

    public BendableLongScore calculateScore(Schedule schedule) {

        //Refer to IncrementalScoreCalculator for description 
        long hard0Score = 0;
        long hard1Score = 0;
        long hard2Score = 0;
        long soft0Score = 0;
        long soft1Score = 0;
        long soft2Score = 0;

        Map<Long, String> batchRoutePathMap = new HashMap<Long, String>();
        Map<Long, Long> batchOtherPenaltyValueMap = new HashMap<Long, Long>();
        Map<Long, Long> batchCurrentPenaltyValueMap = new HashMap<Long, Long>();
        Map<String, Long> segmentOverlapMap = new HashMap<String, Long>();

        if (schedule.getAllocationPathList() != null) {
            for (AllocationPath allocationPath : schedule.getAllocationPathList()) {
                if (allocationPath.getRoutePath() != null) {
                    batchRoutePathMap.put(allocationPath.getBatch().getId(), allocationPath.getRoutePath().getPath());

                    String[] array1 = getSegmentArray(allocationPath.getRoutePath().getPath());

                    for (AllocationPath allocationPath2 : schedule.getAllocationPathList()) {
                        if ((allocationPath2.getRoutePath() != null)
                                && (!(allocationPath.getBatch().equals(allocationPath2.getBatch())))) {
                            for (String str2 : getSegmentArray(allocationPath2.getRoutePath().getPath())) {
                                for (String str1 : array1) {
                                    if (str2.equals(str1)) {
                                        soft2Score += 1;
                                    }
                                }
                            }
                        }
                    }
                }

                batchOtherPenaltyValueMap.put(allocationPath.getBatch().getId(), 0L);
                batchCurrentPenaltyValueMap.put(allocationPath.getBatch().getId(), 0L);
            }
        }

        if (schedule.getAllocationList() != null) {
            for (Allocation allocation : schedule.getAllocationList()) {
                if (batchRoutePathMap.get(allocation.getBatch().getId()) != null) {
                    if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId()))) {
                        if (allocation.getDelay() == null) {
                            batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(),
                                    batchCurrentPenaltyValueMap.get(allocation.getBatch().getId()) + 1);
                        } else if (allocation.getStartInjectionTime() != null) {
                            for (Allocation allocation2 : schedule.getAllocationList()) {
                                if ((allocation2.getDelay() != null)
                                        && (allocation2.getBatch().getId() != allocation.getBatch().getId())
                                        && (batchRoutePathMap.get(allocation2.getBatch().getId()) != null)
                                        && (allocation2.getRoutePath().getPath()
                                                .equals(batchRoutePathMap.get(allocation2.getBatch().getId())))
                                        && (allocation2.getSegment().getName().equals(allocation.getSegment().getName()))) {
                                    Long newOverlapPenaltyValue = 0L;

                                    if ((allocation2.getStartInjectionTime() != null)
                                            && (allocation2.getEndInjectionTime() != null)) {
                                        if ((allocation2.getStartInjectionTime() <= allocation.getStartInjectionTime())
                                                && (allocation2.getEndInjectionTime() >= allocation.getEndInjectionTime())) {
                                            newOverlapPenaltyValue =
                                                    allocation.getEndInjectionTime() - allocation.getStartInjectionTime();
                                        } else if ((allocation2.getStartInjectionTime() >= allocation.getStartInjectionTime())
                                                && (allocation2.getEndInjectionTime() <= allocation.getEndInjectionTime())) {
                                            newOverlapPenaltyValue =
                                                    allocation2.getEndInjectionTime() - allocation2.getStartInjectionTime();
                                        } else if ((allocation2.getStartInjectionTime() <= allocation.getStartInjectionTime())
                                                && (allocation2.getEndInjectionTime() > allocation.getStartInjectionTime())) {
                                            newOverlapPenaltyValue =
                                                    allocation2.getEndInjectionTime() - allocation.getStartInjectionTime();
                                        } else if ((allocation2.getStartInjectionTime() < allocation.getEndInjectionTime())
                                                && (allocation2.getEndInjectionTime() >= allocation.getEndInjectionTime())) {
                                            newOverlapPenaltyValue =
                                                    allocation.getEndInjectionTime() - allocation2.getStartInjectionTime();
                                        }

                                        if ((allocation2.getStartDeliveryTime() <= allocation.getStartDeliveryTime())
                                                && (allocation2.getEndDeliveryTime() >= allocation.getEndDeliveryTime())) {
                                            newOverlapPenaltyValue +=
                                                    allocation.getEndDeliveryTime() - allocation.getStartDeliveryTime();
                                        } else if ((allocation2.getStartDeliveryTime() >= allocation.getStartDeliveryTime())
                                                && (allocation2.getEndDeliveryTime() <= allocation.getEndDeliveryTime())) {
                                            newOverlapPenaltyValue +=
                                                    allocation2.getEndDeliveryTime() - allocation2.getStartDeliveryTime();
                                        } else if ((allocation2.getStartDeliveryTime() <= allocation.getStartDeliveryTime())
                                                && (allocation2.getEndDeliveryTime() > allocation.getStartDeliveryTime())) {
                                            newOverlapPenaltyValue +=
                                                    allocation2.getEndDeliveryTime() - allocation.getStartDeliveryTime();
                                        } else if ((allocation2.getStartDeliveryTime() < allocation.getEndDeliveryTime())
                                                && (allocation2.getEndDeliveryTime() >= allocation.getEndDeliveryTime())) {
                                            newOverlapPenaltyValue +=
                                                    allocation.getEndDeliveryTime() - allocation2.getStartDeliveryTime();
                                        }

                                        if ((allocation2.getStartInjectionTime() >= allocation.getStartInjectionTime())
                                                && (allocation2.getEndDeliveryTime() <= allocation.getEndDeliveryTime())) {
                                            newOverlapPenaltyValue +=
                                                    allocation.getEndDeliveryTime() - allocation2.getEndDeliveryTime();
                                        }

                                        if ((allocation2.getStartInjectionTime() <= allocation.getStartInjectionTime())
                                                && (allocation2.getEndDeliveryTime() >= allocation.getEndDeliveryTime())) {
                                            newOverlapPenaltyValue +=
                                                    allocation2.getEndDeliveryTime() - allocation.getEndDeliveryTime();
                                        }

                                        if (newOverlapPenaltyValue > 0L) {
                                            segmentOverlapMap.put(
                                                    generateCompositeKey(allocation.getSegment().getId().toString(),
                                                            allocation2.getSegment().getId().toString()),
                                                    newOverlapPenaltyValue);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (allocation.getDelay() != null) {
                            batchOtherPenaltyValueMap.put(allocation.getBatch().getId(),
                                    batchOtherPenaltyValueMap.get(allocation.getBatch().getId()) + 1);
                        }
                    }
                } else {
                    batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(),
                            batchCurrentPenaltyValueMap.get(allocation.getBatch().getId()) + 1);
                }
            }
        }

        for (Map.Entry<Long, Long> entry : batchCurrentPenaltyValueMap.entrySet()) {
            if ((entry.getValue() > 0) || (batchRoutePathMap.get(entry.getKey()) == null)) {
                hard0Score -= CURRENT_ALLOCATION_PENALTY;

                if (COMPUTE_hard1Score) {
                    hard1Score -= entry.getValue();
                }
            }
        }

        for (Map.Entry<Long, Long> entry : batchOtherPenaltyValueMap.entrySet()) {
            if ((entry.getValue() > 0) && (batchRoutePathMap.get(entry.getKey()) != null)) {
                hard0Score -= OTHER_ALLOCATION_PENALTY;

                if (COMPUTE_hard1Score) {
                    hard1Score -= entry.getValue();
                }
            }
        }

        for (Map.Entry<String, Long> entry : segmentOverlapMap.entrySet()) {
            hard2Score -= entry.getValue();
        }

        for (Allocation allocation : schedule.getAllocationList()) {
            if (batchRoutePathMap.get(allocation.getBatch().getId()) != null) {
                if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId()))) {
                    if ((allocation.getEndInjectionTime() != null) && (allocation.getEndInjectionTime() > soft0Score)) {
                        soft0Score = allocation.getEndInjectionTime();
                    }
                }
            }
        }

        soft0Score = -soft0Score;

        Map<Long, Long> map1 = new HashMap<Long, Long>();

        for (Allocation allocation : schedule.getAllocationList()) {

            if (batchRoutePathMap.get(allocation.getBatch().getId()) != null) {
                if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId()))) {
                    if ((allocation.getEndInjectionTime() != null)) {
                        if (map1.get(allocation.getBatch().getId()) != null) {
                            if (allocation.getEndInjectionTime() > map1.get(allocation.getBatch().getId())) {
                                map1.put(allocation.getBatch().getId(), allocation.getEndInjectionTime());
                            }
                        } else {
                            map1.put(allocation.getBatch().getId(), allocation.getEndInjectionTime());
                        }
                    }
                }
            }
        }

        for (Map.Entry<Long, Long> entry : map1.entrySet()) {
            soft1Score -= entry.getValue();
        }

        if (COMPUTE_soft2Score) {
            soft2Score = -soft2Score;
        } else {
            soft2Score = 0;
        }

        return BendableLongScore.of(new long[] { hard0Score, hard1Score, hard2Score },
                new long[] { soft0Score, soft1Score, soft2Score });
    }

    public String generateCompositeKey(String key1, String key2) {
        return key1 + "#" + key2;
    }

    public String generateKey(Allocation allocation) {
        return allocation.getBatch().getName() + ":" + allocation.getRoutePath().getPath() + ":"
                + allocation.getSegment().getName();
    }

    public String[] getSegmentArray(String routePath) {
        String[] array1 = routePath.split(ROUTE_PATH_SEPERATOR);

        String[] array2 = new String[array1.length - 1];

        for (int i = 0; i < array2.length; i++) {
            array2[i] = array1[i] + ROUTE_PATH_SEPERATOR + array1[i + 1];
        }

        return array2;
    }

}
