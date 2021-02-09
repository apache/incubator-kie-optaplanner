package org.optaplanner.examples.batchscheduling.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.calculator.IncrementalScoreCalculator;
import org.optaplanner.examples.batchscheduling.domain.Allocation;
import org.optaplanner.examples.batchscheduling.domain.AllocationPath;
import org.optaplanner.examples.batchscheduling.domain.Batch;
import org.optaplanner.examples.batchscheduling.domain.RoutePath;
import org.optaplanner.examples.batchscheduling.domain.Schedule;
import org.optaplanner.examples.batchscheduling.domain.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchSchedulingIncrementalScoreCalculator implements IncrementalScoreCalculator<Schedule, BendableLongScore>
{

	final Logger logger = LoggerFactory.getLogger(BatchSchedulingIncrementalScoreCalculator.class);
	
	public static final String ROUTE_PATH_SEPERATOR = "---";

	// CURRENT_ALLOCATION_PENALTY and OTHER_ALLOCATION_PENALTY are used to compute hard0Score (and not hard1Score) 
	private static final int CURRENT_ALLOCATION_PENALTY = 1;
	private static final int OTHER_ALLOCATION_PENALTY = 1;

	// hard0Score is sum of penalties for all the Batch. It is computed at Batch level.
	// There are 2 types of Penalties.
	//		a) Current Allocation Penalty: 
	//				a1) Set if either RoutePath (i.e Planning variable : routePath in AllocationPath) is not set or
	//				a2) if RoutePath is set but delay is not set for one or more segments(i.e Planning variable delay in Allocation) present in the RoutePath
	//		b) Other Allocation Penalty: Set if RoutePath is not the selected RoutePath but delay is set for a segment in the RoutePath 
    private long hard0Score = 0;

    
	// hard1Score is computed at segment level and not Batch level. It is count of segments that have either Current or Other Allocation penalty defined. 
    // If no RoutePath is defined for a Batch, then it is count of all the segments across all RoutePaths.
    // Once RoutePath is defined, then penalty is only applied (i.e. added) if delay is not set for any segment present in the selected RoutePath and if delay is set for any segment that is not in RoutePath  
    private long hard1Score = 0;
    
    
    // hard2score computes overlap (i.e. time overlap) across batches (i.e. No two bathes should be present in the same segment at the same time) 
    private long hard2Score = 0;

    
    // soft0Score is the time taken to inject first Batch till delivery of last batch 
    private long soft0Score = 0;
    
    // soft1Score is the sum of time taken for all batches (i.e. from injection to delivery)
    // If Schedule contains only a single batch then soft0Score will be same as soft1Score 
    private long soft1Score = 0;
    
    // Count of segments through which commodity has not traversed. 
    // Ideal value is 0 (i.e. All segments have been utilized) 
    private long soft2Score = 0;


    
	private static final boolean COMPUTE_hard1Score = true;
	private static final boolean COMPUTE_soft2Score = true;
	
    private Map<Long, Long> batchOtherPenaltyValueMap;
    private Map<Long, Long> batchCurrentPenaltyValueMap;    
    private Map<Long, Long> batchEndTimeMap;
    private Map<Long, String> batchRoutePathMap;

	private Map<Long, Long> allocationDelayMap;
    private Map<Long, Long> allocationStartTimeMap1;
    private Map<Long, Long> allocationEndTimeMap1;   
    private Map<Long, Long> allocationStartTimeMap2;
    private Map<Long, Long> allocationEndTimeMap2;   
    
    private Map<Long, Segment> segmentMap;
    private Map<String, Long> segmentOverlapMap;

	public String generateCompositeKey(String key1, String key2) 
	{
		return key1 + "#" + key2;
	}

	@Override
	public void resetWorkingSolution(Schedule schedule) 
	{

		batchRoutePathMap = new HashMap<Long, String>();
		segmentMap = new HashMap<Long, Segment>();
        batchOtherPenaltyValueMap = new HashMap<Long, Long>();
        batchCurrentPenaltyValueMap = new HashMap<Long, Long>();
        batchEndTimeMap = new HashMap<Long, Long>(); 
    	segmentOverlapMap = new HashMap<String, Long>();

    	allocationDelayMap = new HashMap<Long, Long>();
    	allocationStartTimeMap1 = new HashMap<Long, Long>();
        allocationEndTimeMap1 = new HashMap<Long, Long>();
    	allocationStartTimeMap2 = new HashMap<Long, Long>();
        allocationEndTimeMap2 = new HashMap<Long, Long>();
        
        
        hard0Score = 0L;
        hard1Score = 0L;
        hard2Score = 0L;
        soft0Score = 0L;
        soft1Score = 0L;
        soft2Score = 0L;

		if (schedule.getBatchList() != null) 
		{
			for (Batch batch : schedule.getBatchList()) 
			{
				batchRoutePathMap.put(batch.getId(), null);
		    	batchOtherPenaltyValueMap.put(batch.getId(), 0L);
		    	batchCurrentPenaltyValueMap.put(batch.getId(), 0L);
				batchEndTimeMap.put(batch.getId(), 0L);
		    	
				for (RoutePath routePath : batch.getRoutePathList()) 
				{
					for (Segment segment : routePath.getSegmentList()) 
					{
						segmentMap.put(segment.getId(), segment);
					}
				}
			}
		}

        if (schedule.getAllocationPathList() != null)
		{	
			for (AllocationPath allocationPath : schedule.getAllocationPathList()) 
			{
	            insert(allocationPath);
	        }
		}

        if (schedule.getAllocationList() != null)
		{	
			for (Allocation allocation : schedule.getAllocationList()) 
			{
	            insert(allocation);
	        }
		}

        printScore("init");
	}

	@Override
	public void beforeEntityAdded(Object entity) 
	{
		// Do Nothing
	}

	@Override
	public void afterEntityAdded(Object entity) 
	{
		// Do Nothing
	}

	@Override
	public void beforeVariableChanged(Object entity, String variableName) 
	{
		if (entity instanceof Allocation)
		{	
			if (!(variableName.equals("predecessorsDoneDate")))
			{
				retract((Allocation) entity);
			} 
			else
			{
				retractPDate((Allocation) entity);				
			}
		}
		else if (entity instanceof AllocationPath)
		{	
			retract((AllocationPath) entity);
		}
	}

	@Override
	public void afterVariableChanged(Object entity, String variableName) 
	{
		if (entity instanceof Allocation)
		{	
			if (!(variableName.equals("predecessorsDoneDate")))
			{
				insert((Allocation) entity);
			}
			else
			{
				insertPDate((Allocation) entity);				
			}

		}
		else if (entity instanceof AllocationPath)
		{	
			insert((AllocationPath) entity);
		}
	}

	@Override
	public void beforeEntityRemoved(Object entity) 
	{
		// Do Nothing
	}

	@Override
	public void afterEntityRemoved(Object entity) 
	{
		// Do Nothing
	}

    public void printScore(String temp) 
    {
    	//logger.debug(temp + ":: " + hard0Score + " " + hard1Score + " " + hard2Score + " / " + soft0Score + " " + soft1Score + " " + soft2Score);
    	//System.out.println(temp + ":: " + hard0Score + " " + hard1Score + " " + hard2Score + " / " + soft0Score + " " + soft1Score + " " + soft2Score);
    	// System.out.println(String.format("%-" + 50 + "." + 50 + "s", temp) + ":: " + hard0Score + " " + hard1Score + " " + hard2Score + " / " + soft0Score + " " + soft1Score + " " + soft2Score);
    }
    
	private void insert(AllocationPath allocationPath) 
	{
        printScore("Insert Start AllocationPath");
		Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocationPath.getBatch().getId());
		Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocationPath.getBatch().getId());
		
		Long newOtherPenaltyValue = 0L ;
		Long newCurrentPenaltyValue = 0L ;
		
		for (Map.Entry<Long, Segment> entry1 : segmentMap.entrySet()) 
		{
			Segment mainSegment = entry1.getValue();
			Long mainDelay = allocationDelayMap.get(entry1.getKey());
			Long mainStartTime1 = allocationStartTimeMap1.get(entry1.getKey());
			Long mainEndTime1 = allocationEndTimeMap1.get(entry1.getKey());
			Long mainStartTime2 = allocationStartTimeMap2.get(entry1.getKey());
			Long mainEndTime2 = allocationEndTimeMap2.get(entry1.getKey());

			// Same Batch
			if (mainSegment.getBatch().getId() == allocationPath.getBatch().getId())
			{
				// If AllocationPath RoutePath is not null, then calculate score after adding RoutePath
				if (allocationPath.getRoutePath() != null)
				{
					if (mainSegment.getRoutePath().getId() == allocationPath.getRoutePath().getId()) 
					{
						if (mainDelay == null) 
						{
							newCurrentPenaltyValue += 1;
						} 
						else if (mainStartTime1 != null)
						{	
							for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
							{
								if 	((allocationDelayMap.get(entry2.getKey()) != null) && (entry2.getValue().getBatch().getId() != allocationPath.getBatch().getId()) && (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) != null) && (entry2.getValue().getRoutePath().getPath().equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId()))) && (entry2.getValue().getName().equals(mainSegment.getName())) && (allocationStartTimeMap1.get(entry1.getKey()) != null) && (allocationEndTimeMap1.get(entry1.getKey()) != null))
								{	
									//String otherKey = entry2.getKey();
									Long newOverlapPenaltyValue = 0L;
		
									if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) >= mainEndTime1))
									{
										newOverlapPenaltyValue = mainEndTime1 - mainStartTime1;
									} 
									else if ((allocationStartTimeMap1.get(entry2.getKey()) >= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) <= mainEndTime1))
									{
										newOverlapPenaltyValue = allocationEndTimeMap1.get(entry2.getKey()) - allocationStartTimeMap1.get(entry2.getKey());
									}	
									else if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) > mainStartTime1))
									{
										newOverlapPenaltyValue = allocationEndTimeMap1.get(entry2.getKey()) - mainStartTime1;
									}	
									else if ((allocationStartTimeMap1.get(entry2.getKey()) < mainEndTime1) && (allocationEndTimeMap1.get(entry2.getKey()) >= mainEndTime1))
									{
										newOverlapPenaltyValue = mainEndTime1 - allocationStartTimeMap1.get(entry2.getKey());
									}
									
									if ((allocationStartTimeMap2.get(entry2.getKey()) <= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
									{
										newOverlapPenaltyValue += mainEndTime2 - mainStartTime2;
									} 
									else if ((allocationStartTimeMap2.get(entry2.getKey()) >= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) <= mainEndTime2))
									{
										newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - allocationStartTimeMap2.get(entry2.getKey());
									}	
									else if ((allocationStartTimeMap2.get(entry2.getKey()) <= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) > mainStartTime2))
									{
										newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - mainStartTime2;
									}	
									else if ((allocationStartTimeMap2.get(entry2.getKey()) < mainEndTime2) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
									{
										newOverlapPenaltyValue += mainEndTime2 - allocationStartTimeMap2.get(entry2.getKey());
									}

									if ((allocationStartTimeMap1.get(entry2.getKey()) >= mainStartTime1) && (allocationEndTimeMap2.get(entry2.getKey()) <= mainEndTime2))
									{
										newOverlapPenaltyValue += mainEndTime2 - allocationEndTimeMap2.get(entry2.getKey());
									} 

									if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
									{
										newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - mainEndTime2;
									} 
									
									if (newOverlapPenaltyValue > 0L)
									{	
										hard2Score -= (2 * newOverlapPenaltyValue);
										segmentOverlapMap.put(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString()) , newOverlapPenaltyValue);
										segmentOverlapMap.put(generateCompositeKey(entry2.getKey().toString(), entry1.getKey().toString()) , newOverlapPenaltyValue);
									}
								}	
							}	
						}
					}
					// Same Batch Different RoutePath from the Preferred RoutePath
					else 
					{
						if (mainDelay != null) 
						{
							newOtherPenaltyValue += 1;
						}
					}
				} 
				else
				{
					newCurrentPenaltyValue += 1;
				}
			}
		}

		// Penalize (per Batch) if Segment in other route Path have been allocated
		if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) 
		{
			hard0Score -= OTHER_ALLOCATION_PENALTY;
			// System.out.println("Adding OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
		} 
		else if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) 
		{
			hard0Score += OTHER_ALLOCATION_PENALTY;
			// System.out.println("Removing OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
		}

		// Penalize (per Batch) if any Segment in same route Path has not been allocated
		if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) 
		{
			hard0Score -= CURRENT_ALLOCATION_PENALTY;
			// System.out.println("Adding CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
		} 
		else if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) 
		{
			hard0Score += CURRENT_ALLOCATION_PENALTY;
			// System.out.println("Removing CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
		}
			
		if (allocationPath.getRoutePath() != null)
		{	
			batchRoutePathMap.put(allocationPath.getBatch().getId(), allocationPath.getRoutePath().getPath());
		}
		else
		{
			batchRoutePathMap.remove(allocationPath.getBatch().getId());
		}

		updateBatchEndDate(allocationPath);

		batchOtherPenaltyValueMap.put(allocationPath.getBatch().getId(),newOtherPenaltyValue );
		batchCurrentPenaltyValueMap.put(allocationPath.getBatch().getId(),newCurrentPenaltyValue);
		
		if (COMPUTE_hard1Score)
		{	
			hard1Score =  hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue +  oldOtherPenaltyValue + oldCurrentPenaltyValue ;
		} 
		else
		{
			hard1Score =  0;
		}
		
		soft0Score = - getMaxEndTime();
		soft1Score = - getTotalProjectTime();

		if (COMPUTE_soft2Score)
		{
			soft2Score = - computeRoutePathSegmentOverlap();
		}
		else
		{
			soft2Score = 0;
		}
			
        printScore("Insert End AllocationPath");
	}
	
	private void retract(AllocationPath allocationPath) 
	{

        printScore("Retract Start AllocationPath");

		Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocationPath.getBatch().getId());
		Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocationPath.getBatch().getId());
		Long newOtherPenaltyValue = 0L ;
		Long newCurrentPenaltyValue = 0L ;

		for (Map.Entry<Long, Segment> entry1 : segmentMap.entrySet()) 
		{
			//String mainKey = entry1.getKey();
			Segment mainSegment = entry1.getValue();

			// Same Batch
			if (mainSegment.getBatch().getId() == allocationPath.getBatch().getId()) 
			{
				newCurrentPenaltyValue = newCurrentPenaltyValue + 1;

				for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
				{
					Segment otherSegment = entry2.getValue();
					
					if 	((otherSegment.getBatch().getId() != allocationPath.getBatch().getId()) && (otherSegment.getName().equals(mainSegment.getName())) && (segmentOverlapMap.get(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString())) != null))
					{	
						hard2Score += (2 * segmentOverlapMap.get(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString())));
						segmentOverlapMap.remove(generateCompositeKey(entry1.getKey().toString(), entry2.getKey().toString()));
						segmentOverlapMap.remove(generateCompositeKey(entry2.getKey().toString(), entry1.getKey().toString()));
					}	
				}	
			}
		}

		// Penalize (per Batch) if Segment in other route Path have been allocated
		if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) 
		{
			hard0Score -= OTHER_ALLOCATION_PENALTY;
			// System.out.println("Adding OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
		} 
		else if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) 
		{
			hard0Score += OTHER_ALLOCATION_PENALTY;
			// System.out.println("Removing OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
		}

		// Penalize (per Batch) if any Segment in same route Path has not been allocated
		if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) 
		{
			hard0Score -= CURRENT_ALLOCATION_PENALTY;
			// System.out.println("Adding CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
		} 
		else if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) 
		{
			hard0Score += CURRENT_ALLOCATION_PENALTY;
			// System.out.println("Removing CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
		}
			
		batchRoutePathMap.remove(allocationPath.getBatch().getId());
		batchOtherPenaltyValueMap.put(allocationPath.getBatch().getId(),newOtherPenaltyValue );
		batchCurrentPenaltyValueMap.put(allocationPath.getBatch().getId(),newCurrentPenaltyValue);
		
		if (COMPUTE_hard1Score)
		{	
			hard1Score =  hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue +  oldOtherPenaltyValue + oldCurrentPenaltyValue ;
		} 
		else
		{
			hard1Score =  0;
		}

        printScore("Retract End AllocationPath");
	} 

	private void insert(Allocation allocation) 
	{

        printScore("Insert Start Allocation");

		if (batchRoutePathMap.get(allocation.getBatch().getId()) != null) 
		{
			Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocation.getBatch().getId());
			Long newCurrentPenaltyValue = oldCurrentPenaltyValue;
			Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocation.getBatch().getId());
			Long newOtherPenaltyValue = oldOtherPenaltyValue;
			Long mainStartTime1 = allocation.getStartInjectionTime();
			Long mainEndTime1 = allocation.getEndInjectionTime();
			Long mainStartTime2 = allocation.getStartDeliveryTime();
			Long mainEndTime2 = allocation.getEndDeliveryTime();
			
			if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId())))
			{
				if ((allocation.getDelay() != null) && (allocationDelayMap.get(allocation.getSegment().getId()) == null))
				{
					newCurrentPenaltyValue = newCurrentPenaltyValue - 1;
				
					if (mainStartTime1 != null)
					{	
						for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
						{
							if 	((allocationDelayMap.get(entry2.getKey()) != null) && (entry2.getValue().getBatch().getId() != allocation.getBatch().getId()) && (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) != null)  && (entry2.getValue().getRoutePath().getPath().equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId()))) && (entry2.getValue().getName().equals(allocation.getSegment().getName())) && (allocationStartTimeMap1.get(entry2.getKey()) != null) && (allocationEndTimeMap1.get(entry2.getKey()) != null))
							{	
								Long newOverlapPenaltyValue = 0L;
		
								if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) >= mainEndTime1))
								{
									newOverlapPenaltyValue = mainEndTime1 - mainStartTime1;
								} 
								else if ((allocationStartTimeMap1.get(entry2.getKey()) >= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) <= mainEndTime1))
								{
									newOverlapPenaltyValue = allocationEndTimeMap1.get(entry2.getKey()) - allocationStartTimeMap1.get(entry2.getKey());
								}	
								else if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) > mainStartTime1))
								{
									newOverlapPenaltyValue = allocationEndTimeMap1.get(entry2.getKey()) - mainStartTime1;
								}	
								else if ((allocationStartTimeMap1.get(entry2.getKey()) < mainEndTime1) && (allocationEndTimeMap1.get(entry2.getKey()) >= mainEndTime1))
								{
									newOverlapPenaltyValue = mainEndTime1 - allocationStartTimeMap1.get(entry2.getKey());
								}
								
								if ((allocationStartTimeMap2.get(entry2.getKey()) <= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
								{
									newOverlapPenaltyValue += mainEndTime2 - mainStartTime2;
								} 
								else if ((allocationStartTimeMap2.get(entry2.getKey()) >= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) <= mainEndTime2))
								{
									newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - allocationStartTimeMap2.get(entry2.getKey());
								}	
								else if ((allocationStartTimeMap2.get(entry2.getKey()) <= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) > mainStartTime2))
								{
									newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - mainStartTime2;
								}	
								else if ((allocationStartTimeMap2.get(entry2.getKey()) < mainEndTime2) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
								{
									newOverlapPenaltyValue += mainEndTime2 - allocationStartTimeMap2.get(entry2.getKey());
								}

								if ((allocationStartTimeMap1.get(entry2.getKey()) >= mainStartTime1) && (allocationEndTimeMap2.get(entry2.getKey()) <= mainEndTime2))
								{
									newOverlapPenaltyValue += mainEndTime2 - allocationEndTimeMap2.get(entry2.getKey());
								} 

								if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
								{
									newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - mainEndTime2;
								} 

								if (newOverlapPenaltyValue > 0L)
								{	
									hard2Score -= (2 * newOverlapPenaltyValue);
									segmentOverlapMap.put(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString()) , newOverlapPenaltyValue);
									segmentOverlapMap.put(generateCompositeKey(entry2.getKey().toString(), allocation.getSegment().getId().toString()) , newOverlapPenaltyValue);
								}	
							}	
						}	
					}	
				} 
				else if ((allocation.getDelay() == null) && (allocationDelayMap.get(allocation.getSegment().getId()) != null))
				{
					newCurrentPenaltyValue = newCurrentPenaltyValue + 1;
				}
			} 
			else
			{
				if ((allocation.getDelay() != null) && (allocationDelayMap.get(allocation.getSegment().getId()) == null))
				{
					newOtherPenaltyValue = newOtherPenaltyValue + 1;
				}
				else if ((allocation.getDelay() != null) && (allocationDelayMap.get(allocation.getSegment().getId()) == null))
				{
					newOtherPenaltyValue = newOtherPenaltyValue - 1;
				}
			}

			// Penalize (per Batch) if Segment in other route Path have been allocated
			if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) 
			{
				hard0Score -= OTHER_ALLOCATION_PENALTY;
				// System.out.println("Adding OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
			} 
			else if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) 
			{
				hard0Score += OTHER_ALLOCATION_PENALTY;
				// System.out.println("Removing OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
			}
	
			// Penalize (per Batch) if any Segment in same route Path has not been allocated
			if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) 
			{
				hard0Score -= CURRENT_ALLOCATION_PENALTY;
				// System.out.println("Adding CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
			} 
			else if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) 
			{
				hard0Score += CURRENT_ALLOCATION_PENALTY;
				// System.out.println("Removing CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
			}
				
			allocationDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
			allocationStartTimeMap1.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
			allocationEndTimeMap1.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
			allocationStartTimeMap2.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
			allocationEndTimeMap2.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());

			
			updateBatchEndDate(allocation);

			batchOtherPenaltyValueMap.put(allocation.getBatch().getId(),newOtherPenaltyValue );
			batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(),newCurrentPenaltyValue);

			if (COMPUTE_hard1Score)
			{	
				hard1Score =  hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue +  oldOtherPenaltyValue + oldCurrentPenaltyValue ;
			} 
			else
			{
				hard1Score =  0;
			}
			soft0Score = - getMaxEndTime();	
			soft1Score = - getTotalProjectTime();
		}	
		else
		{	
			allocationDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
			allocationStartTimeMap1.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
			allocationEndTimeMap1.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
			allocationStartTimeMap2.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
			allocationEndTimeMap2.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());
		}	

        printScore("Insert End Allocation");
	}

	private void insertPDate(Allocation allocation) 
	{

        if (batchRoutePathMap.get(allocation.getBatch().getId()) != null) 
		{
			Long mainStartTime1 = allocation.getStartInjectionTime();
			Long mainEndTime1 = allocation.getEndInjectionTime();
			Long mainStartTime2 = allocation.getStartDeliveryTime();
			Long mainEndTime2 = allocation.getEndDeliveryTime();
			
			if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId())))
			{
				if ((allocation.getDelay() != null) && (allocationDelayMap.get(allocation.getSegment().getId()) == null))
				{
					if (mainStartTime1 != null)
					{	
						for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
						{
							if 	((allocationDelayMap.get(entry2.getKey()) != null) &&  (entry2.getValue().getBatch().getId() != allocation.getBatch().getId()) && (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) != null)  && (entry2.getValue().getRoutePath().getPath().equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId()))) && (entry2.getValue().getName().equals(allocation.getSegment().getName())) && (allocationStartTimeMap1.get(entry2.getKey()) != null) && (allocationEndTimeMap1.get(entry2.getKey()) != null))
							{	
								Long newOverlapPenaltyValue = 0L;
		
								if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) >= mainEndTime1))
								{
									newOverlapPenaltyValue = mainEndTime1 - mainStartTime1;
								} 
								else if ((allocationStartTimeMap1.get(entry2.getKey()) >= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) <= mainEndTime1))
								{
									newOverlapPenaltyValue = allocationEndTimeMap1.get(entry2.getKey()) - allocationStartTimeMap1.get(entry2.getKey());
								}	
								else if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap1.get(entry2.getKey()) > mainStartTime1))
								{
									newOverlapPenaltyValue = allocationEndTimeMap1.get(entry2.getKey()) - mainStartTime1;
								}	
								else if ((allocationStartTimeMap1.get(entry2.getKey()) < mainEndTime1) && (allocationEndTimeMap1.get(entry2.getKey()) >= mainEndTime1))
								{
									newOverlapPenaltyValue = mainEndTime1 - allocationStartTimeMap1.get(entry2.getKey());
								}
								
								if ((allocationStartTimeMap2.get(entry2.getKey()) <= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
								{
									newOverlapPenaltyValue += mainEndTime2 - mainStartTime2;
								} 
								else if ((allocationStartTimeMap2.get(entry2.getKey()) >= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) <= mainEndTime2))
								{
									newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - allocationStartTimeMap2.get(entry2.getKey());
								}	
								else if ((allocationStartTimeMap2.get(entry2.getKey()) <= mainStartTime2) && (allocationEndTimeMap2.get(entry2.getKey()) > mainStartTime2))
								{
									newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - mainStartTime2;
								}	
								else if ((allocationStartTimeMap2.get(entry2.getKey()) < mainEndTime2) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
								{
									newOverlapPenaltyValue += mainEndTime2 - allocationStartTimeMap2.get(entry2.getKey());
								}

								if ((allocationStartTimeMap1.get(entry2.getKey()) >= mainStartTime1) && (allocationEndTimeMap2.get(entry2.getKey()) <= mainEndTime2))
								{
									newOverlapPenaltyValue += mainEndTime2 - allocationEndTimeMap2.get(entry2.getKey());
								} 

								if ((allocationStartTimeMap1.get(entry2.getKey()) <= mainStartTime1) && (allocationEndTimeMap2.get(entry2.getKey()) >= mainEndTime2))
								{
									newOverlapPenaltyValue += allocationEndTimeMap2.get(entry2.getKey()) - mainEndTime2;
								} 

								if (newOverlapPenaltyValue > 0L)
								{	
									hard2Score -= (2 * newOverlapPenaltyValue);
									segmentOverlapMap.put(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString()) , newOverlapPenaltyValue);
									segmentOverlapMap.put(generateCompositeKey(entry2.getKey().toString(), allocation.getSegment().getId().toString()) , newOverlapPenaltyValue);
								}	
							}	
						}	
					}	
				} 
			} 

			allocationDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
			allocationStartTimeMap1.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
			allocationEndTimeMap1.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
			allocationStartTimeMap2.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
			allocationEndTimeMap2.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());
			updateBatchEndDate(allocation);

			soft0Score = - getMaxEndTime();	
			soft1Score = - getTotalProjectTime();
		}	
		else
		{	
			allocationDelayMap.put(allocation.getSegment().getId(), allocation.getDelay());
			allocationStartTimeMap1.put(allocation.getSegment().getId(), allocation.getStartInjectionTime());
			allocationEndTimeMap1.put(allocation.getSegment().getId(), allocation.getEndInjectionTime());
			allocationStartTimeMap2.put(allocation.getSegment().getId(), allocation.getStartDeliveryTime());
			allocationEndTimeMap2.put(allocation.getSegment().getId(), allocation.getEndDeliveryTime());
		}	

	}

	private void retract(Allocation allocation) 
	{

        printScore("Retract Start Allocation");

		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
		{
			Segment otherSegment = entry2.getValue();
			
			if 	((otherSegment.getBatch().getId() != allocation.getBatch().getId()) && (otherSegment.getName().equals(allocation.getSegment().getName())) && (segmentOverlapMap.get(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString())) != null))
			{	
				hard2Score += (2 * segmentOverlapMap.get(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString())));
				segmentOverlapMap.remove(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString()));
				segmentOverlapMap.remove(generateCompositeKey(entry2.getKey().toString(), allocation.getSegment().getId().toString()));
			}	
		}	

		if (batchRoutePathMap.get(allocation.getBatch().getId()) != null) 
		{
			Long oldCurrentPenaltyValue = batchCurrentPenaltyValueMap.get(allocation.getBatch().getId());
			Long newCurrentPenaltyValue = oldCurrentPenaltyValue;
			Long oldOtherPenaltyValue = batchOtherPenaltyValueMap.get(allocation.getBatch().getId());
			Long newOtherPenaltyValue = oldOtherPenaltyValue;
			
			if (allocation.getRoutePath().getPath().equals(batchRoutePathMap.get(allocation.getBatch().getId())))
			{
				if  ((allocation.getDelay() != null) && (allocationDelayMap.get(allocation.getSegment().getId()) != null))
				{
					newCurrentPenaltyValue = newCurrentPenaltyValue + 1;
				}
			} 
			else
			{
				if  ((allocation.getDelay() != null) && (allocationDelayMap.get(allocation.getSegment().getId()) != null))
				{
					newOtherPenaltyValue = newOtherPenaltyValue - 1;
				}
			}
			
			// Penalize (per Batch) if Segment in other route Path have been allocated
			if ((oldOtherPenaltyValue == 0) && (newOtherPenaltyValue > 0)) 
			{
				hard0Score -= OTHER_ALLOCATION_PENALTY;
				// System.out.println("Adding OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
			} 
			else if ((oldOtherPenaltyValue > 0) && (newOtherPenaltyValue == 0)) 
			{
				hard0Score += OTHER_ALLOCATION_PENALTY;
				// System.out.println("Removing OTHER_ALLOCATION_PENALTY(" + OTHER_ALLOCATION_PENALTY + ")");
			}
	
			// Penalize (per Batch) if any Segment in same route Path has not been allocated
			if ((oldCurrentPenaltyValue == 0) && (newCurrentPenaltyValue > 0)) 
			{
				hard0Score -= CURRENT_ALLOCATION_PENALTY;
				// System.out.println("Adding CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
			} 
			else if ((oldCurrentPenaltyValue > 0) && (newCurrentPenaltyValue == 0)) 
			{
				hard0Score += CURRENT_ALLOCATION_PENALTY;
				// System.out.println("Removing CURRENT_ALLOCATION_PENALTY(" + CURRENT_ALLOCATION_PENALTY + ")");
			}
				
			batchOtherPenaltyValueMap.put(allocation.getBatch().getId(),newOtherPenaltyValue );
			batchCurrentPenaltyValueMap.put(allocation.getBatch().getId(),newCurrentPenaltyValue);

			allocationDelayMap.remove(allocation.getSegment().getId());
			allocationStartTimeMap1.remove(allocation.getSegment().getId());
			allocationEndTimeMap1.remove(allocation.getSegment().getId());
			allocationStartTimeMap2.remove(allocation.getSegment().getId());
			allocationEndTimeMap2.remove(allocation.getSegment().getId());

			if (COMPUTE_hard1Score)
			{	
				hard1Score =  hard1Score - newOtherPenaltyValue - newCurrentPenaltyValue +  oldOtherPenaltyValue + oldCurrentPenaltyValue ;
			} 
			else
			{
				hard1Score =  0;
			}
		} 
		else	
		{	
			allocationDelayMap.remove(allocation.getSegment().getId());
			allocationStartTimeMap1.remove(allocation.getSegment().getId());
			allocationEndTimeMap1.remove(allocation.getSegment().getId());
			allocationStartTimeMap2.remove(allocation.getSegment().getId());
			allocationEndTimeMap2.remove(allocation.getSegment().getId());
		}	

        printScore("Retract End Allocation");

	}
	
	private void retractPDate(Allocation allocation) 
	{
		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
		{
			Segment otherSegment = entry2.getValue();
			
			if 	((otherSegment.getBatch().getId() != allocation.getBatch().getId()) && (otherSegment.getName().equals(allocation.getSegment().getName())) && (segmentOverlapMap.get(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString())) != null))
			{	
				hard2Score += (2 * segmentOverlapMap.get(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString())));
				segmentOverlapMap.remove(generateCompositeKey(allocation.getSegment().getId().toString(), entry2.getKey().toString()));
				segmentOverlapMap.remove(generateCompositeKey(entry2.getKey().toString(), allocation.getSegment().getId().toString()));
			}	
		}	

		allocationDelayMap.remove(allocation.getSegment().getId());
		allocationStartTimeMap1.remove(allocation.getSegment().getId());
		allocationEndTimeMap1.remove(allocation.getSegment().getId());
		allocationStartTimeMap2.remove(allocation.getSegment().getId());
		allocationEndTimeMap2.remove(allocation.getSegment().getId());

	}


	public void updateBatchEndDate(AllocationPath allocationPath) 
	{
		Long maxEndTime = 0L;
		
		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
		{
			if 	((entry2.getValue().getBatch().getId() == allocationPath.getBatch().getId()) && (allocationEndTimeMap1.get(entry2.getKey()) != null) && (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) != null) && (entry2.getValue().getRoutePath().getPath().equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId()))))
			{
				if ((Long)(allocationEndTimeMap1.get(entry2.getKey())) > maxEndTime) 
				{
					maxEndTime = (Long)(allocationEndTimeMap1.get(entry2.getKey()));
				}
			}
		}	

		batchEndTimeMap.put(allocationPath.getBatch().getId(), maxEndTime);
	}
	
	public void updateBatchEndDate(Allocation allocation) 
	{

		Long maxEndTime = 0L;
		
		for (Map.Entry<Long, Segment> entry2 : segmentMap.entrySet()) 
		{
			if 	((entry2.getValue().getBatch().getId() == allocation.getBatch().getId()) && (allocationEndTimeMap1.get(entry2.getKey()) != null) && (batchRoutePathMap.get(entry2.getValue().getBatch().getId()) != null) && (entry2.getValue().getRoutePath().getPath().equals(batchRoutePathMap.get(entry2.getValue().getBatch().getId()))))
			{
				if ((Long)(allocationEndTimeMap1.get(entry2.getKey())) > maxEndTime) 
				{
					maxEndTime = (Long)(allocationEndTimeMap1.get(entry2.getKey()));
				}
			}
		}	

		batchEndTimeMap.put(allocation.getBatch().getId(), maxEndTime);
	}
	
	public long getMaxEndTime() 
	{
		long maxEndTime = 0L;

		for (Map.Entry<Long, Long> entry : batchEndTimeMap.entrySet()) 
		{
			if ((entry.getValue() != null) && ((Long) (entry.getValue()) > maxEndTime)) 
			{
				maxEndTime = (Long) entry.getValue();
			}
		}

		return maxEndTime;
	}

	public long getTotalProjectTime() 
	{
		long totalProjectTime = 0L;

		for (Map.Entry<Long, Long> entry : batchEndTimeMap.entrySet()) 
		{
			if ((entry.getValue() != null)) 
			{
				totalProjectTime = totalProjectTime + (Long) entry.getValue();
			}
		}

		return totalProjectTime;
	}

	private long computeRoutePathSegmentOverlap()
	{
		long long1 = 0;
		for (Map.Entry<Long, String> allocationPath : batchRoutePathMap.entrySet()) 
		{
			if (allocationPath.getValue() != null)
			{	
				String[] array1 = getSegmentArray(allocationPath.getValue());
				
				for (Map.Entry<Long, String> allocationPath2 : batchRoutePathMap.entrySet()) 
				{
					if ((allocationPath2.getValue() != null) && (!(allocationPath.getKey().equals(allocationPath2.getKey()))))
					{	
						for (String str2 : getSegmentArray(allocationPath2.getValue())) 
						{
							for (String str1 : array1) 
							{
								if (str2.equals(str1))
								{
									long1 += 1;
								}
							}
						}
					}
				}
			}
		}
	
		return long1; 
	}

	public String[] getSegmentArray(String routePath) 
	{
		String[] array1 = routePath.split(ROUTE_PATH_SEPERATOR);
		
		String[] array2 = new String[array1.length - 1];
		
		for (int i=0; i < array2.length; i++)
		{	
			array2[i] = array1[i] + ROUTE_PATH_SEPERATOR + array1[i+1]; 
		}

		return array2;
	}

	public BendableLongScore calculateScore() {
		return BendableLongScore.of(new long[] {hard0Score, hard1Score, hard2Score}, new long[] { soft0Score, soft1Score, soft2Score });
	}
}
