package org.optaplanner.examples.batchscheduling.domain.solver;

import java.util.ArrayDeque;
import java.util.Queue;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.examples.batchscheduling.domain.Allocation;
import org.optaplanner.examples.batchscheduling.domain.Schedule;

public class PredecessorsDoneDateUpdatingVariableListener implements VariableListener<Schedule , Allocation> {

	@Override
	public void beforeEntityAdded(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

	@Override
	public void afterEntityAdded(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
		updateAllocation(scoreDirector, allocation);
    }

	@Override
	public void beforeVariableChanged(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

	@Override
	public void afterVariableChanged(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        //Commented on 20th March
		updateAllocation(scoreDirector, allocation);
    }

	@Override
	public void beforeEntityRemoved(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

	@Override
    public void afterEntityRemoved(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

	protected void updateAllocation(ScoreDirector<Schedule> scoreDirector, Object arg1) 
    {
        
		Allocation originalAllocation = (Allocation) arg1;
		
		Queue<Allocation> uncheckedSuccessorQueue = new ArrayDeque<Allocation>();
        
        		
        Allocation tempAllocation = originalAllocation;
        while (tempAllocation.getSuccessorAllocation() != null)
        {
        	uncheckedSuccessorQueue.add(tempAllocation.getSuccessorAllocation());
        	tempAllocation = tempAllocation.getSuccessorAllocation();
        }

        while (!uncheckedSuccessorQueue.isEmpty()) 
        {
	        Allocation allocation = uncheckedSuccessorQueue.remove();
	        Long predecessorStartDate2 = 0L;
	        
	        if (allocation.getPredecessorAllocation() != null)
	        {	
	        	predecessorStartDate2 = allocation.getPredecessorAllocation().getStartDeliveryTime();
		        scoreDirector.beforeVariableChanged(allocation, "predecessorsDoneDate");
		        allocation.setPredecessorsDoneDate(predecessorStartDate2);
		        scoreDirector.afterVariableChanged(allocation, "predecessorsDoneDate");
	        }
        }
    }

}
