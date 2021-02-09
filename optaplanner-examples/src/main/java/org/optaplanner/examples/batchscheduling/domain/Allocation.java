package org.optaplanner.examples.batchscheduling.domain;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;
import org.optaplanner.examples.batchscheduling.domain.solver.DelayStrengthComparator;
import org.optaplanner.examples.batchscheduling.domain.solver.PredecessorsDoneDateUpdatingVariableListener;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@PlanningEntity
@XStreamAlias("PipeAllocation")
public class Allocation extends AbstractPersistable {

	
	// Determines rounding logic. If fractional value is more than specified number (i.e. 20), then the value is rounded off to next higher value.
	// Minimum value is 0 and maximum 100
	public static final Integer FRACTIONAL_VOLUME_PERCENTAGE = 20; 
	
	
	// Scaling parameter. Lower value indicates more processing time but accurate result.
	// Minimum value 1. No  Maximum defined but it depends on the volume present in the batches.
	// Note: This is an example of process manufacturing (not discrete manufacturing). As such, variables (i.e. batch volume) needs to be split into quantifiable unit. 
    public static final Integer PERIODINTERVAL_IN_MINUTES = 5;
    
	private Batch batch;
    private RoutePath routePath;
    private Segment segment;
    private Allocation predecessorAllocation;
    private Allocation successorAllocation;


	// Planning variables: changes during planning, between score calculations.
	private Long delay; 
   
	// Shadow variables
	private Long predecessorsDoneDate;
   
	public Batch getBatch() {
		return batch;
	}

	public void setBatch(Batch batch) {
		this.batch = batch;
	}

	public RoutePath getRoutePath() {
		return routePath;
	}

	public void setRoutePath(RoutePath routePath) {
		this.routePath = routePath;
	}

	public Segment getSegment() {
		return segment;
	}

    public void setSegment(Segment segment) {
        this.segment = segment;
    }
	
    public Allocation getPredecessorAllocation() {
		return predecessorAllocation;
	}

	public void setPredecessorAllocation(Allocation predecessorAllocation) {
		this.predecessorAllocation = predecessorAllocation;
	}

	public Allocation getSuccessorAllocation() {
		return successorAllocation;
	}

	public void setSuccessorAllocation(Allocation successorAllocation) {
		this.successorAllocation = successorAllocation;
	}

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public void setPredecessorsDoneDate(Long predecessorsDoneDate) {
        this.predecessorsDoneDate = predecessorsDoneDate;
    }

    // Start of Injection
    public Long getStartInjectionTime() 
    {
    	if (delay == null) 
        {
            return null;
        }
        
    	if (predecessorAllocation != null)
    	{	
	    	if  (!(predecessorsDoneDate == null))
	    	{
	    		return (delay + predecessorsDoneDate);		
	    	} 
    	} else
    	{
    		return delay;
    	}
    	
    	return null;
    }

    // End of Injection
    public Long getEndInjectionTime() {
   		if (getStartInjectionTime() == null) {
            return null;
        }

    	//Injection Time
		Double tempInjectionDouble = new Double((getBatch().getVolume()/(getSegment().getFlowRate() * Allocation.PERIODINTERVAL_IN_MINUTES)));
		Long tempInjectionLong = tempInjectionDouble.longValue();
		int fractionalInjectionInt = (int)Math.round((tempInjectionDouble - tempInjectionLong) * 100);
		
		if (fractionalInjectionInt >= Allocation.FRACTIONAL_VOLUME_PERCENTAGE  )
		{	
			return getStartInjectionTime() + tempInjectionLong + 1;
		}
		else
		{
			return getStartInjectionTime() + tempInjectionLong;
		}
    }

    // Start of Delivery
    // Start of Delivery is Start of Injection Time + Travel Time
    public Long getStartDeliveryTime() 
    {
    	if (getStartInjectionTime() == null) {
            return null;
        }

    	//Travel Time
		Double tempTravelDouble = new Double(((getSegment().getLength() * getSegment().getCrossSectionArea())/(getSegment().getFlowRate() * Allocation.PERIODINTERVAL_IN_MINUTES)));
		Long tempTravelLong = tempTravelDouble.longValue();
		int fractionalTravelInt = (int)Math.round((tempTravelDouble - tempTravelLong) * 100);
		
		if (fractionalTravelInt >= Allocation.FRACTIONAL_VOLUME_PERCENTAGE  )
		{	
			return getStartInjectionTime() + tempTravelLong + 1;
		}
		else
		{
			return getStartInjectionTime() + tempTravelLong;
		}
    }
    
    // End of Delivery
    // End of Delivery is End of Injection Time + Travel Time
    public Long getEndDeliveryTime() {

    	if (getStartInjectionTime() == null) {
            return null;
        }

    	//Travel Time
		Double tempTravelDouble = new Double(((getSegment().getLength() * getSegment().getCrossSectionArea())/(getSegment().getFlowRate() * Allocation.PERIODINTERVAL_IN_MINUTES)));
		Long tempTravelLong = tempTravelDouble.longValue();
		int fractionalTravelInt = (int)Math.round((tempTravelDouble - tempTravelLong) * 100);
		
		if (fractionalTravelInt >= Allocation.FRACTIONAL_VOLUME_PERCENTAGE  )
		{	
			tempTravelLong = getStartInjectionTime() + tempTravelLong + 1;
		}
		else
		{
			tempTravelLong = getStartInjectionTime() + tempTravelLong;
		}

    	//Injection Time
		Double tempDeliveryDouble = new Double((getBatch().getVolume()/(getSegment().getFlowRate() * Allocation.PERIODINTERVAL_IN_MINUTES)));
		Long tempDeliveryLong = tempDeliveryDouble.longValue();
		int fractionalDeliveryInt = (int)Math.round((tempDeliveryDouble - tempDeliveryLong) * 100);
		
		if (fractionalDeliveryInt >= Allocation.FRACTIONAL_VOLUME_PERCENTAGE  )
		{	
			return tempTravelLong + tempDeliveryLong + 1;
		}
		else
		{
			return tempTravelLong + tempDeliveryLong;
		}
    }

    public String getLabel() 
    {
        return "Label:: " + batch.getName() + " " + routePath.getPath() + " " + segment.getName();
    }
   
    public Boolean isFirst() 
	{
		if (predecessorAllocation != null)
		{	
			return new Boolean(false);
		}
		else
		{	
			return new Boolean(true);
		}	
	}

	@PlanningVariable(nullable=true, valueRangeProviderRefs = {"delayRange"},  strengthComparatorClass = DelayStrengthComparator.class)
    public Long getDelay() {
        return delay;
    }

    @CustomShadowVariable(variableListenerClass = PredecessorsDoneDateUpdatingVariableListener.class, sources = {@PlanningVariableReference(variableName = "delay")})
    public Long getPredecessorsDoneDate() {
        return predecessorsDoneDate;
    }

    @ValueRangeProvider(id = "delayRange")
    public CountableValueRange<Long> getDelayRange() 
    {
    	// Delay Range
    	if (getBatch().getDelayRangeValue() != null)
    	{	
    		return ValueRangeFactory.createLongValueRange(0, getBatch().getDelayRangeValue());
    	}
    	else
    	{
    		return ValueRangeFactory.createLongValueRange(0, 2000);
    	}
    }

}
