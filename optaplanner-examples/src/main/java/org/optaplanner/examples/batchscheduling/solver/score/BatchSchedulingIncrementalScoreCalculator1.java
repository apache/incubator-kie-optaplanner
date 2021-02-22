package org.optaplanner.examples.batchscheduling.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.calculator.IncrementalScoreCalculator;
import org.optaplanner.examples.batchscheduling.app.BatchSchedulingApp;
import org.optaplanner.examples.batchscheduling.domain.Allocation;
import org.optaplanner.examples.batchscheduling.domain.AllocationPath;
import org.optaplanner.examples.batchscheduling.domain.Batch;
import org.optaplanner.examples.batchscheduling.domain.RoutePath;
import org.optaplanner.examples.batchscheduling.domain.Schedule;
import org.optaplanner.examples.batchscheduling.domain.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchSchedulingIncrementalScoreCalculator
		implements IncrementalScoreCalculator<Schedule, BendableLongScore> {

	final Logger logger = LoggerFactory.getLogger(BatchSchedulingIncrementalScoreCalculator.class);

	// hard0Score is sum of penalties for all the Batches. It is computed at Batch
	// level. There are 2 types of Penalties.
	//
	// a) SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY:
	// a1) Applicable if RoutePath is selected for a Batch but delay is not set for
	// one or more segments present in the RoutePath
	// OR
	// a2) if RoutePath is not set for a Batch
	//
	// b) NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY:
	// Set if RoutePath is not the selected RoutePath but delay is set for a segment
	// in the RoutePath
	private long hard0Score = 0;

	// hard1Score is computed at segment level (i.e. not at Batch level). It helps
	// arriving at result faster because it operates at segment level.
	//
	// a) For scenarios where RoutePath is selected, it is computed by adding
	// following 2 segment counts:
	// a1) Count of segments which are part of RoutePath but delay is not set and
	// a2) Count of segments which are not part of RoutePath but delay is set
	//
	// b) For scenarios where RoutePath is not selected for a Batch, it is the count
	// of all segments across all the RoutePaths for the given Batch.
	private long hard1Score = 0;

	// hard2score computes overlap (i.e. time overlap) across batches (i.e. No two
	// bathes should be present in the same segment at the same time)
	private long hard2Score = 0;

	// soft0Score is the time taken to inject first Batch till delivery of last
	// batch
	private long soft0Score = 0;

	// Count of segments through which commodity has not traversed.
	// Ideal value is 0 (i.e. All segments have been utilized)
	// This soft score helps all routePaths to be utilized.
	private long soft1Score = 0;

	// Map to store BatchId and selectedRoutePath for that Batch
	private Map<Long, String> batchRoutePathMap;

	// Map to store BatchId and count of segments that are part of selectedRoutePath
	// but don't have delay assigned
	private Map<Long, Long> batchCurrentPenaltyValueMap;

	// Map to store BatchId and count of segments that are not part of
	// selectedRoutePath but have delay assigned
	private Map<Long, Long> batchOtherPenaltyValueMap;

	// Map to store Maximum EndTime for every batch
	private Map<Long, Long> batchEndTimeMap;

	// Stores mapping between segmentId and Delay value
	private Map<Long, Long> segmentDelayMap;

	// For calculating hardscore2
	// This map will always contain even number of entries (i.e. If Segment A
	// overlaps B, then B also overlaps A)
	private Map<String, Long> segmentOverlapMap;

	// Following 4 Maps contain mapping between SegmentId and different timings
	// Used for determining overlap values
	private Map<Long, Long> segmentStartInjectionTimeMap;
	private Map<Long, Long> segmentEndInjectionTimeMap;
	private Map<Long, Long> segmentStartDeliveryTimeMap;
	private Map<Long, Long> segmentEndDeliveryTimeMap;

	private Map<Long, Segment> segmentMap;

	// List to store unique segmentString (not segmentId). Boolean field is not
	// used.
	private Map<String, Boolean> segmentStringMap;

	public static String generateCompositeKey(String key1, String key2) {
		return key1 + "#" + key2;
	}

	@Override
	public void resetWorkingSolution(Schedule schedule) {

		batchRoutePathMap = new HashMap<Long, String>();
		segmentMap = new HashMap<Long, Segment>();
		segmentStringMap = new HashMap<String, Boolean>();
		batchOtherPenaltyValueMap = new HashMap<Long, Long>();
		batchCurrentPenaltyValueMap = new HashMap<Long, Long>();
		batchEndTimeMap = new HashMap<Long, Long>();
		segmentOverlapMap = new HashMap<String, Long>();

		segmentDelayMap = new HashMap<Long, Long>();
		segmentStartInjectionTimeMap = new HashMap<Long, Long>();
		segmentEndInjectionTimeMap = new HashMap<Long, Long>();
		segmentStartDeliveryTimeMap = new HashMap<Long, Long>();
		segmentEndDeliveryTimeMap = new HashMap<Long, Long>();

		hard0Score = 0L;
		hard1Score = 0L;
		hard2Score = 0L;
		soft0Score = 0L;
		soft1Score = 0L;

		for (Batch batch : schedule.getBatchList()) {
			batchRoutePathMap.put(batch.getId(), null);
			batchOtherPenaltyValueMap.put(batch.getId(), 0L);
			batchCurrentPenaltyValueMap.put(batch.getId(), 0L);
			batchEndTimeMap.put(batch.getId(), 0L);

			for (RoutePath routePath : batch.getRoutePathList()) {
				for (Segment segment : routePath.getSegmentList()) {
					segmentMap.put(segment.getId(), segment);
					segmentStringMap.put(segment.getName(), true);
				}
			}
		}

		for (AllocationPath allocationPath : schedule.getAllocationPathList()) {
			insert(allocationPath);
		}

		for (Allocation allocation : schedule.getAllocationList()) {
			insert(allocation);
		}

		printScore("init");
	}

	@Override
	public void beforeEntityAdded(Object entity) {
		// Do Nothing
	}

	@Override
	public void afterEntityAdded(Object entity) {
		// Do Nothing
	}

	@Override
	public void beforeVariableChanged(Object entity, String variableName) {
		if (entity instanceof Allocation) {
			if (!(variableName.equals("predecessorsDoneDate"))) {
				retract((Allocation) entity);
			} else {
				retractPredecessorDate((Allocation) entity);
			}
		} else if (entity instanceof AllocationPath) {
			retract((AllocationPath) entity);
		}
	}

	@Override
	public void afterVariableChanged(Object entity, String variableName) {
		if (entity instanceof Allocation) {
			if (!(variableName.equals("predecessorsDoneDate"))) {
				insert((Allocation) entity);
			} else {
				insertPredecessorDate((Allocation) entity);
			}

		} else if (entity instanceof AllocationPath) {
			insert((AllocationPath) entity);
		}
	}

	@Override
	public void beforeEntityRemoved(Object entity) {
		// Do Nothing
	}

	@Override
	public void afterEntityRemoved(Object entity) {
		// Do Nothing
	}

	public void printScore(String temp) {
		logger.debug(String.format("%-" + 50 + "." + 50 + "s", temp) + ":: " + hard0Score + " " + hard1Score + " "
				+ hard2Score + " / " + soft0Score + " " + soft1Score);
	}

	// Compute Penalties and Overlap
	private void insert(AllocationPath allocationPath) {
		printScore("Insert Start AllocationPath");

		// Get existing current and other penalty values for the batch
		Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocationPath.getBatch().getId());
		Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocationPath.getBatch().getId());

		Long newOtherPenaltyValue = 0L;
		Long newCurrentPenaltyValue = 0L;

		// Start of compute overlap
		for (Map.Entry<Long, Segment> entry1 : segmentMap.entrySet()) {

			Segment outerSegment = entry1.getValue();
			Long outerSegmentDelay = segmentDelayMap.get(entry1.getKey());
			Long outerSegmentStartInjectionTime = segmentStartInjectionTimeMap.get(entry1.getKey());

			// Continue if Segment Batch is not same as the Input Parameter Batch
			if (outerSegment.getBatch().getId() != allocationPath.getBatch().getId()) {
				continue;
			}

			// Continue if RoutePath is set to null
			if (allocationPath.getRoutePath() == null) {
				newCurrentPenaltyValue += 1;
				continue;
			}

			// Continue if Segment RoutePath is different from the Input Parameter RoutePath
			if (outerSegment.getRoutePath().getId() != allocationPath.getRoutePath().getId()) {

				if (outerSegmentDelay != null) {
					newOtherPenaltyValue += 1;
				}
				continue;
			}

			if (outerSegmentDelay == null) {
				newCurrentPenaltyValue += 1;
				continue;
			}

			if (outerSegmentStartInjectionTime == null) {
				continue;
			}

			Long outerSegmentEndInjectionTime = segmentEndInjectionTimeMap.get(entry1.getKey());
			Long outerSegmentStartDeliveryTime = segmentStartDeliveryTimeMap.get(entry1.getKey());
			Long outerSegmentEndDeliveryTime = segmentEndDeliveryTimeMap.get(entry1.getKey());

			for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {

				// Continue if Segment delay is not set
				if (segmentDelayMap.get(entry2.getKey()) == null) {
					continue;
				}

				// Continue if Segment Batch is not same as the Input Parameter Batch
				if (entry2.getValue().getBatch().getId() == allocationPath.getBatch().getId()) {
					continue;
				}

				// Continue if RoutePath has not been selected for the Batch
				if (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) == null) {
					continue;
				}

				// Continue if Segment is not part of the selectedRoutepath
				if (!(entry2.getValue().getRoutePath().getPath()
						.equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId())))) {
					continue;
				}

				// Continue if Inner Segment is same as the Outer Segment
				if (entry2.getValue().getId() != outerSegment.getId()) {
					continue;
				}

				// Continue if 
				if (segmentStartInjectionTimeMap.get(entry1.getKey()) == null) {
					continue;
				}

				Long newOverlapPenaltyValue = 0L;

				// Check for following 4 conditions for Injection:
				// Condition 1) If inner segment injection start time is less than outer segment
				// injection start time and inner segment injection end date is more than outer
				// segment injection end time
				// Condition 2) If inner segment injection start time is more than outer segment
				// injection start time and inner segment injection end date is less than outer
				// segment injection end time
				// Condition 3) If inner segment injection start time is less than outer segment
				// injection start time and inner segment injection end date is more than outer
				// segment injection start time
				// Condition 4) If inner segment injection start time is less than outer segment
				// injection end time and inner segment injection end date is more than outer
				// segment injection end time
				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= outerSegmentStartInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) >= outerSegmentEndInjectionTime)) {
					newOverlapPenaltyValue = outerSegmentEndInjectionTime - outerSegmentStartInjectionTime;
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) >= outerSegmentStartInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) <= outerSegmentEndInjectionTime)) {
					newOverlapPenaltyValue = segmentEndInjectionTimeMap.get(entry2.getKey())
							- segmentStartInjectionTimeMap.get(entry2.getKey());
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= outerSegmentStartInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) > outerSegmentStartInjectionTime)) {
					newOverlapPenaltyValue = segmentEndInjectionTimeMap.get(entry2.getKey())
							- outerSegmentStartInjectionTime;
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) < outerSegmentEndInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) >= outerSegmentEndInjectionTime)) {
					newOverlapPenaltyValue = outerSegmentEndInjectionTime
							- segmentStartInjectionTimeMap.get(entry2.getKey());
				}

				// Check for 4 overlap conditions for Delivery
				if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) <= outerSegmentStartDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= outerSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += outerSegmentEndDeliveryTime - outerSegmentStartDeliveryTime;
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) >= outerSegmentStartDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) <= outerSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey())
							- segmentStartDeliveryTimeMap.get(entry2.getKey());
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) <= outerSegmentStartDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) > outerSegmentStartDeliveryTime)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey())
							- outerSegmentStartDeliveryTime;
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) < outerSegmentEndDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= outerSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += outerSegmentEndDeliveryTime
							- segmentStartDeliveryTimeMap.get(entry2.getKey());
				}

				// Check for overlap scenario where inner segment Injection start time is more
				// than outer segment Injection start time and inner segment delivery end time
				// is less than outer segment Delivery end time and
				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) >= outerSegmentStartInjectionTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) <= outerSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += outerSegmentEndDeliveryTime
							- segmentEndDeliveryTimeMap.get(entry2.getKey());
				}

				// Check for overlap scenario where inner segment Injection start time is less
				// than outer segment Injection start time and inner segment delivery end time
				// is more than outer segment Delivery end time and
				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= outerSegmentStartInjectionTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= outerSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey())
							- outerSegmentEndDeliveryTime;
				}

				if (newOverlapPenaltyValue > 0L) {
					hard2Score -= (2 * newOverlapPenaltyValue);
					segmentOverlapMap.put(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString()),
							newOverlapPenaltyValue);
					segmentOverlapMap.put(generateCompositeKey(entry2.getKey().toString(), entry1.getKey().toString()),
							newOverlapPenaltyValue);
				}
			}
		}
		// End of compute overlap

		// Apply new penalty if any segment in nonSelectedRoutePath has delay assigned
		// Applicable if no NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Remove existing penalty if any segment in nonSelectedRoutePath has delay
		// assigned
		// Applicable only if NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Apply penalty if any segment in selectedRoutePath has delay not assigned
		// Applicable if no SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		// Remove penalty if all segments in selectedRoutePath have delay assigned
		// Applicable only if SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		if (allocationPath.getRoutePath() != null) {
			batchRoutePathMap.put(allocationPath.getBatch().getId(), allocationPath.getRoutePath().getPath());
		} else {
			batchRoutePathMap.remove(allocationPath.getBatch().getId());
		}

		updateBatchEndDate(allocationPath);

		// Compute hardscore1
		batchOtherPenaltyValueMap.put(allocationPath.getBatch().getId(), newOtherPenaltyValue);
		batchCurrentPenaltyValueMap.put(allocationPath.getBatch().getId(), newCurrentPenaltyValue);
		hard1Score = hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue + oldOtherPenaltyValue
				+ oldCurrentPenaltyValue;

		soft0Score = -getMaxEndTime();
		soft1Score = -computeRoutePathSegmentOverlap();

		printScore("Insert End AllocationPath");
	}

	// Compute Penalties and Overlap
	private void retract(AllocationPath allocationPath) {

		printScore("Retract Start AllocationPath");

		// Get existing current and other penalty values for the batch
		Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocationPath.getBatch().getId());
		Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocationPath.getBatch().getId());

		Long newOtherPenaltyValue = 0L;
		Long newCurrentPenaltyValue = 0L;

		// Start of compute overlap
		for (Map.Entry<Long, Segment> entry1 : segmentMap.entrySet()) {

			// Continue if Segment Batch is not same as Input Batch
			if (entry1.getValue().getBatch().getId() != allocationPath.getBatch().getId()) {
				continue;
			}

			newCurrentPenaltyValue = newCurrentPenaltyValue + 1;

			for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {

				// Continue if Segment Batch is not same as the Input Batch
				if (entry2.getValue().getBatch().getId() == allocationPath.getBatch().getId()) {
					continue;
				}

				// Continue if Inner Segment is same as the Outer Segment
				if (!(entry2.getValue().getName().equals(entry1.getValue().getName()))) {
					continue;
				}

				// Check if overlap exists in the map. If no overlap exists then continue.
				if (segmentOverlapMap
						.get(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString())) == null) {
					continue;
				}

				// If overlap exists then remove the overlap score from hard2score. Also remove
				// the overlap from the Map.
				// Notice the multiplication factor of 2 because if A overlaps B, then B also
				// overlaps A
				hard2Score += (2 * segmentOverlapMap
						.get(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString())));
				segmentOverlapMap.remove(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString()));
				segmentOverlapMap.remove(generateCompositeKey(entry2.getKey().toString(), entry1.getKey().toString()));
			}
		}
		// End of compute overlap

		// Apply new penalty if any segment in nonSelectedRoutePath has delay assigned
		// Applicable if no NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Remove existing penalty if any segment in nonSelectedRoutePath has delay
		// assigned
		// Applicable only if NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Apply penalty if any segment in selectedRoutePath has delay not assigned
		// Applicable if no SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		// Remove penalty if all segments in selectedRoutePath have delay assigned
		// Applicable only if SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		batchRoutePathMap.remove(allocationPath.getBatch().getId());
		batchOtherPenaltyValueMap.put(allocationPath.getBatch().getId(), newOtherPenaltyValue);
		batchCurrentPenaltyValueMap.put(allocationPath.getBatch().getId(), newCurrentPenaltyValue);
		hard1Score = hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue + oldOtherPenaltyValue
				+ oldCurrentPenaltyValue;

		printScore("Retract End AllocationPath");
	}

	private void insert(Allocation allocation) {

		printScore("Insert Start Allocation");

		// If RoutePath is not set for the Batch then update Map values and return
		if (batchRoutePathMap.get(allocation.getBatch().getId()) == null) {
			segmentDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
			segmentStartInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
			segmentEndInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
			segmentStartDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
			segmentEndDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());
			printScore("Insert End Allocation");
			return;
		}

		Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocation.getBatch().getId());
		Long newCurrentPenaltyValue = oldCurrentPenaltyValue;
		Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocation.getBatch().getId());
		Long newOtherPenaltyValue = oldOtherPenaltyValue;

		Long inputSegmentStartInjectionTime = allocation.getStartInjectionTime();

		// If input segment is not part of the selectedRoutePath, then compare previous
		// delay value with new delay value.
		// If previous value is null but new value is not null, then add penalty for
		// that segment
		// else if previous value is not null but new value is null, then remove penalty
		// for that segment
		if (!(allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId())))) {
			if ((allocation.getDelay() != null) && (segmentDelayMap.get(allocation.getSegment().getId()) == null)) {
				newOtherPenaltyValue = newOtherPenaltyValue + 1;
			} else if ((allocation.getDelay() != null)
					&& (segmentDelayMap.get(allocation.getSegment().getId()) == null)) {
				newOtherPenaltyValue = newOtherPenaltyValue - 1;
			}
		}

		// If input segment is part of the selectedRoutePath, then compare previous
		// delay value with new delay value.
		// If previous value is not null but new value is null, then add penalty for
		// that segment
		// else if previous value is null but new value is not null, then remove penalty
		// for that segment
		if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId()))) {
			if ((allocation.getDelay() != null) && (segmentDelayMap.get(allocation.getSegment().getId()) != null)) {
				if ((allocation.getDelay() == null) && (segmentDelayMap.get(allocation.getSegment().getId()) != null)) {
					newCurrentPenaltyValue = newCurrentPenaltyValue + 1;
				} else if ((allocation.getDelay() != null)
						&& (segmentDelayMap.get(allocation.getSegment().getId()) == null)) {
					newCurrentPenaltyValue = newCurrentPenaltyValue - 1;
				}
			}
		}

		boolean computeOverLap = false;

		// Determine if overlap needs to be computed. 
		// Overlap is computed if input segment is part of the selectedRoutePath and delay and Injection start time are not null
		if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId()))) {
			if ((allocation.getDelay() != null) && (segmentDelayMap.get(allocation.getSegment().getId()) == null)) {
				if (inputSegmentStartInjectionTime != null) {
					computeOverLap = true;
				}
			}
		}

		// Start of compute overlap
		if (computeOverLap) {

			Long inputSegmentEndInjectionTime = allocation.getEndInjectionTime();
			Long inputSegmentStartDeliveryTime = allocation.getStartDeliveryTime();
			Long inputSegmentEndDeliveryTime = allocation.getEndDeliveryTime();

			for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {

				//Continue if Segment delay is not set
				if (segmentDelayMap.get(entry2.getKey()) == null) {
					continue;
				}

				//Continue if Segment Batch is not same as the Input Parameter Batch
				if (entry2.getValue().getBatch().getId() == allocation.getBatch().getId()) {
					continue;
				}

				//Continue if RoutePath has not been selected for the Batch
				if (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) == null) {
					continue;
				}

				//Continue if Segment is not part of the selectedRoutepath
				if (!(entry2.getValue().getRoutePath().getPath()
						.equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId())))) {
					continue;
				}

				//Continue if Segment is same as the Input Parameter Segment 
				if (!(entry2.getValue().getName().equals(allocation.getSegment().getName()))) {
					continue;
				}

				if (segmentStartInjectionTimeMap.get(entry2.getKey()) == null) {
					continue;
				}

				Long newOverlapPenaltyValue = 0L;

				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= inputSegmentStartInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) >= inputSegmentEndInjectionTime)) {
					newOverlapPenaltyValue = inputSegmentEndInjectionTime - inputSegmentStartInjectionTime;
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) >= inputSegmentStartInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) <= inputSegmentEndInjectionTime)) {
					newOverlapPenaltyValue = segmentEndInjectionTimeMap.get(entry2.getKey())
							- segmentStartInjectionTimeMap.get(entry2.getKey());
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= inputSegmentStartInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) > inputSegmentStartInjectionTime)) {
					newOverlapPenaltyValue = segmentEndInjectionTimeMap.get(entry2.getKey())
							- inputSegmentStartInjectionTime;
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) < inputSegmentEndInjectionTime)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) >= inputSegmentEndInjectionTime)) {
					newOverlapPenaltyValue = inputSegmentEndInjectionTime
							- segmentStartInjectionTimeMap.get(entry2.getKey());
				}

				if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) <= inputSegmentStartDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= inputSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += inputSegmentEndDeliveryTime - inputSegmentStartDeliveryTime;
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) >= inputSegmentStartDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) <= inputSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey())
							- segmentStartDeliveryTimeMap.get(entry2.getKey());
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) <= inputSegmentStartDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) > inputSegmentStartDeliveryTime)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey())
							- inputSegmentStartDeliveryTime;
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) < inputSegmentEndDeliveryTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= inputSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += inputSegmentEndDeliveryTime
							- segmentStartDeliveryTimeMap.get(entry2.getKey());
				}

				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) >= inputSegmentStartInjectionTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) <= inputSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += inputSegmentEndDeliveryTime
							- segmentEndDeliveryTimeMap.get(entry2.getKey());
				}

				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= inputSegmentStartInjectionTime)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= inputSegmentEndDeliveryTime)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey())
							- inputSegmentEndDeliveryTime;
				}

				if (newOverlapPenaltyValue > 0L) {
					hard2Score -= (2 * newOverlapPenaltyValue);
					segmentOverlapMap.put(generateCompositeKey(allocation.getSegment().getId().toString(),
							entry2.getKey().toString()), newOverlapPenaltyValue);
					segmentOverlapMap.put(generateCompositeKey(entry2.getKey().toString(),
							allocation.getSegment().getId().toString()), newOverlapPenaltyValue);
				}
			}
		}
		// End of compute overlap

		// Apply new penalty if any segment in nonSelectedRoutePath has delay assigned
		// Applicable if no NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Remove existing penalty if any segment in nonSelectedRoutePath has delay
		// assigned
		// Applicable only if NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Apply penalty if any segment in selectedRoutePath has delay not assigned
		// Applicable if no SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		// Remove penalty if all segments in selectedRoutePath have delay assigned
		// Applicable only if SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		segmentDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
		segmentStartInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
		segmentEndInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
		segmentStartDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
		segmentEndDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());

		updateBatchEndDate(allocation);

		batchOtherPenaltyValueMap.put(allocation.getBatch().getId(), newOtherPenaltyValue);
		batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(), newCurrentPenaltyValue);
		hard1Score = hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue + oldOtherPenaltyValue
				+ oldCurrentPenaltyValue;
		soft0Score = -getMaxEndTime();

		printScore("Insert End Allocation");
	}

	private void insertPredecessorDate(Allocation allocation) {

		if (batchRoutePathMap.get(allocation.getBatch().getId()) == null) {

			segmentDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
			segmentStartInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
			segmentEndInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
			segmentStartDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
			segmentEndDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());

			return;
		}

		boolean computeOverlap = false;

		if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId()))) {
			if ((allocation.getDelay() != null) && (segmentDelayMap.get(allocation.getSegment().getId()) == null)) {
				if (allocation.getStartInjectionTime() != null) {
					computeOverlap = true;
				}
			}
		}

		if (computeOverlap) {
			Long mainStartTime1 = allocation.getStartInjectionTime();
			Long mainEndTime1 = allocation.getEndInjectionTime();
			Long mainStartTime2 = allocation.getStartDeliveryTime();
			Long mainEndTime2 = allocation.getEndDeliveryTime();

			for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {

				if (segmentDelayMap.get(entry2.getKey()) == null) {
					continue;
				}

				if (entry2.getValue().getBatch().getId() == allocation.getBatch().getId()) {
					continue;
				}

				if (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) == null) {
					continue;
				}

				if (!(entry2.getValue().getRoutePath().getPath()
						.equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId())))) {
					continue;
				}

				if (!(entry2.getValue().getName().equals(allocation.getSegment().getName()))) {
					continue;
				}

				if (segmentStartInjectionTimeMap.get(entry2.getKey()) == null) {
					continue;
				}

				if (segmentEndInjectionTimeMap.get(entry2.getKey()) == null) {
					continue;
				}

				Long newOverlapPenaltyValue = 0L;

				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= mainStartTime1)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) >= mainEndTime1)) {
					newOverlapPenaltyValue = mainEndTime1 - mainStartTime1;
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) >= mainStartTime1)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) <= mainEndTime1)) {
					newOverlapPenaltyValue = segmentEndInjectionTimeMap.get(entry2.getKey())
							- segmentStartInjectionTimeMap.get(entry2.getKey());
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= mainStartTime1)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) > mainStartTime1)) {
					newOverlapPenaltyValue = segmentEndInjectionTimeMap.get(entry2.getKey()) - mainStartTime1;
				} else if ((segmentStartInjectionTimeMap.get(entry2.getKey()) < mainEndTime1)
						&& (segmentEndInjectionTimeMap.get(entry2.getKey()) >= mainEndTime1)) {
					newOverlapPenaltyValue = mainEndTime1 - segmentStartInjectionTimeMap.get(entry2.getKey());
				}

				if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) <= mainStartTime2)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= mainEndTime2)) {
					newOverlapPenaltyValue += mainEndTime2 - mainStartTime2;
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) >= mainStartTime2)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) <= mainEndTime2)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey())
							- segmentStartDeliveryTimeMap.get(entry2.getKey());
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) <= mainStartTime2)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) > mainStartTime2)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey()) - mainStartTime2;
				} else if ((segmentStartDeliveryTimeMap.get(entry2.getKey()) < mainEndTime2)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= mainEndTime2)) {
					newOverlapPenaltyValue += mainEndTime2 - segmentStartDeliveryTimeMap.get(entry2.getKey());
				}

				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) >= mainStartTime1)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) <= mainEndTime2)) {
					newOverlapPenaltyValue += mainEndTime2 - segmentEndDeliveryTimeMap.get(entry2.getKey());
				}

				if ((segmentStartInjectionTimeMap.get(entry2.getKey()) <= mainStartTime1)
						&& (segmentEndDeliveryTimeMap.get(entry2.getKey()) >= mainEndTime2)) {
					newOverlapPenaltyValue += segmentEndDeliveryTimeMap.get(entry2.getKey()) - mainEndTime2;
				}

				if (newOverlapPenaltyValue > 0L) {
					hard2Score -= (2 * newOverlapPenaltyValue);
					segmentOverlapMap.put(generateCompositeKey(allocation.getSegment().getId().toString(),
							entry2.getKey().toString()), newOverlapPenaltyValue);
					segmentOverlapMap.put(generateCompositeKey(entry2.getKey().toString(),
							allocation.getSegment().getId().toString()), newOverlapPenaltyValue);
				}
			}
		}

		segmentDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
		segmentStartInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
		segmentEndInjectionTimeMap.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
		segmentStartDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
		segmentEndDeliveryTimeMap.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());
		updateBatchEndDate(allocation);

		soft0Score = -getMaxEndTime();
	}

	private void retract(Allocation allocation) {

		printScore("Retract Start Allocation");

		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {

			if (entry2.getValue().getBatch().getId() == allocation.getBatch().getId()) {
				continue;
			}

			if (!(entry2.getValue().getName().equals(allocation.getSegment().getName()))) {
				continue;
			}

			if ((segmentOverlapMap.get(generateCompositeKey(allocation.getSegment().getId().toString(),
					entry2.getKey().toString())) == null)) {
				continue;
			}

			hard2Score += (2 * segmentOverlapMap
					.get(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString())));
			segmentOverlapMap.remove(
					generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString()));
			segmentOverlapMap.remove(
					generateCompositeKey(entry2.getKey().toString(), allocation.getSegment().getId().toString()));
		}

		if (batchRoutePathMap.get(allocation.getBatch().getId()) == null) {
			segmentDelayMap.remove(allocation.getSegment().getId());
			segmentStartInjectionTimeMap.remove(allocation.getSegment().getId());
			segmentEndInjectionTimeMap.remove(allocation.getSegment().getId());
			segmentStartDeliveryTimeMap.remove(allocation.getSegment().getId());
			segmentEndDeliveryTimeMap.remove(allocation.getSegment().getId());
			printScore("Retract End Allocation");
			return;
		}

		Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocation.getBatch().getId());
		Long newCurrentPenaltyValue = oldCurrentPenaltyValue;
		Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocation.getBatch().getId());
		Long newOtherPenaltyValue = oldOtherPenaltyValue;

		if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId()))) {
			if ((allocation.getDelay() != null) && (segmentDelayMap.get(allocation.getSegment().getId()) != null)) {
				newCurrentPenaltyValue = newCurrentPenaltyValue + 1;
			}
		} else {
			if ((allocation.getDelay() != null) && (segmentDelayMap.get(allocation.getSegment().getId()) != null)) {
				newOtherPenaltyValue = newOtherPenaltyValue - 1;
			}
		}

		// Apply new penalty if any segment in nonSelectedRoutePath has delay assigned
		// Applicable if no NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Remove existing penalty if any segment in nonSelectedRoutePath has delay
		// assigned
		// Applicable only if NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.NON_SELECTED_ROUTEPATH_ALLOCATION_PENALTY;
		}

		// Apply penalty if any segment in selectedRoutePath has delay not assigned
		// Applicable if no SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) {
			hard0Score -= BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		// Remove penalty if all segments in selectedRoutePath have delay assigned
		// Applicable only if SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY exists (for the
		// batch)
		if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) {
			hard0Score += BatchSchedulingApp.SELECTED_ROUTEPATH_NON_ALLOCATION_PENALTY;
		}

		batchOtherPenaltyValueMap.put(allocation.getBatch().getId(), newOtherPenaltyValue);
		batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(), newCurrentPenaltyValue);

		segmentDelayMap.remove(allocation.getSegment().getId());
		segmentStartInjectionTimeMap.remove(allocation.getSegment().getId());
		segmentEndInjectionTimeMap.remove(allocation.getSegment().getId());
		segmentStartDeliveryTimeMap.remove(allocation.getSegment().getId());
		segmentEndDeliveryTimeMap.remove(allocation.getSegment().getId());
		hard1Score = hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue + oldOtherPenaltyValue
				+ oldCurrentPenaltyValue;

		printScore("Retract End Allocation");

	}

	private void retractPredecessorDate(Allocation allocation) {
		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {

			if (entry2.getValue().getBatch().getId() == allocation.getBatch().getId()) {
				continue;
			}

			if (!(entry2.getValue().getName().equals(allocation.getSegment().getName()))) {
				continue;
			}

			if (segmentOverlapMap.get(generateCompositeKey(allocation.getSegment().getId().toString(),
					entry2.getKey().toString())) == null) {
				continue;
			}

			hard2Score += (2 * segmentOverlapMap
					.get(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString())));
			segmentOverlapMap.remove(
					generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString()));
			segmentOverlapMap.remove(
					generateCompositeKey(entry2.getKey().toString(), allocation.getSegment().getId().toString()));

		}

		segmentDelayMap.remove(allocation.getSegment().getId());
		segmentStartInjectionTimeMap.remove(allocation.getSegment().getId());
		segmentEndInjectionTimeMap.remove(allocation.getSegment().getId());
		segmentStartDeliveryTimeMap.remove(allocation.getSegment().getId());
		segmentEndDeliveryTimeMap.remove(allocation.getSegment().getId());

	}

	public void updateBatchEndDate(AllocationPath allocationPath) {
		Long maxEndTime = 0L;

		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {

			if (segmentEndDeliveryTimeMap.get(entry2.getKey()) == null) {
				continue;
			}

			if (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) == null) {
				continue;
			}

			if (entry2.getValue().getBatch().getId() != allocationPath.getBatch().getId()) {
				continue;
			}

			if (!(entry2.getValue().getRoutePath().getPath()
					.equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId())))) {
				continue;
			}

			if ((Long) (segmentEndDeliveryTimeMap.get(entry2.getKey())) <= maxEndTime) {
				continue;
			}

			maxEndTime = (Long) (segmentEndDeliveryTimeMap.get(entry2.getKey()));

		}

		batchEndTimeMap.put(allocationPath.getBatch().getId(), maxEndTime);
	}

	public void updateBatchEndDate(Allocation allocation) {

		Long maxEndTime = 0L;

		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) {
			if (segmentEndDeliveryTimeMap.get(entry2.getKey()) == null) {
				continue;
			}

			if (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) == null) {
				continue;
			}

			if (entry2.getValue().getBatch().getId() != allocation.getBatch().getId()) {
				continue;
			}

			if (!(entry2.getValue().getRoutePath().getPath()
					.equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId())))) {
				continue;
			}

			if ((Long) (segmentEndDeliveryTimeMap.get(entry2.getKey())) <= maxEndTime) {
				continue;
			}

			maxEndTime = (Long) (segmentEndDeliveryTimeMap.get(entry2.getKey()));

		}

		batchEndTimeMap.put(allocation.getBatch().getId(), maxEndTime);
	}

	public long getMaxEndTime() {
		long maxEndTime = 0L;

		for (Map.Entry<Long, Long> entry : batchEndTimeMap.entrySet()) {

			if (entry.getValue() == null) {
				continue;
			}

			if ((Long) (entry.getValue()) <= maxEndTime) {
				continue;
			}

			maxEndTime = (Long) entry.getValue();
		}

		return maxEndTime;
	}

	private long computeRoutePathSegmentOverlap() {
		Map<String, Boolean> segmentMap1 = new HashMap<String, Boolean>();

		for (Map.Entry<Long, String> allocationPath : batchRoutePathMap.entrySet()) {

			if (allocationPath.getValue() == null) {
				continue;
			}

			for (String s : RoutePath.getSegmentArray(allocationPath.getValue())) {
				segmentMap1.put(s, true);
			}
		}

		return segmentStringMap.size() - segmentMap1.size();
	}

	public BendableLongScore calculateScore() {
		return BendableLongScore.of(new long[] { hard0Score, hard1Score, hard2Score },
				new long[] { soft0Score, soft1Score });
	}
}
