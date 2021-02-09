package org.optaplanner.examples.batchscheduling.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.persistence.xstream.api.score.buildin.bendablelong.BendableLongScoreXStreamConverter;

@PlanningSolution
@XStreamAlias("PipeSchedule")
public class Schedule extends AbstractPersistable {

	private List<Batch> batchList;
    private List<RoutePath> routePathList;
    private List<Segment> segmentList;

    private List<Allocation> allocationList;
    private List<AllocationPath> allocationPathList;

    @XStreamConverter(BendableLongScoreXStreamConverter.class)
    private BendableLongScore score;

    @ProblemFactCollectionProperty
    public List<Batch> getBatchList() {
        return batchList;
    }
    
    public List<Segment> getSegmentList() {
		return segmentList;
	}

	public void setSegment(List<Segment> segmentList) {
		this.segmentList = segmentList;
	}

    public List<RoutePath> getRoutePathList() {
        return routePathList;
    }

	public void setBatchList(List<Batch> batchList) {
        this.batchList = batchList;
    }

    @PlanningEntityCollectionProperty
    public List<Allocation> getAllocationList() {
        return allocationList;
    }

    public void setAllocationList(List<Allocation> allocationList) {
        this.allocationList = allocationList;
    }

    @PlanningEntityCollectionProperty
    public List<AllocationPath> getAllocationPathList() {
		return allocationPathList;
	}

	public void setAllocationPathList(List<AllocationPath> allocationPathList) {
		this.allocationPathList = allocationPathList;
	}
	
	@PlanningScore(bendableHardLevelsSize = 3, bendableSoftLevelsSize = 3)
    public BendableLongScore getScore() {
        return score;
    }

    public void setScore(BendableLongScore score) {
        this.score = score;
    }

    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        return facts;
    }

}
