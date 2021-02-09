package org.optaplanner.examples.batchscheduling.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;


import org.optaplanner.examples.common.domain.AbstractPersistable;

@XStreamAlias("PipeSegment")
public class Segment extends AbstractPersistable {

	private Batch batch;
	private RoutePath routePath;
	private String name;
	private Integer sequence; 

	//flowRate is in m3/minute
	private float flowRate;
	
	private float length;
	private float crossSectionArea;
	
    public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public Batch getBatch() {
		return batch;
	}

	public void setBatch(Batch batch) {
		this.batch = batch;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RoutePath getRoutePath() {
		return routePath;
	}

	public void setRoutePath(RoutePath routePath) {
		this.routePath = routePath;
	}

	public float getFlowRate() {
		return flowRate;
	}

	public void setFlowRate(float flowRate) {
		this.flowRate = flowRate;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public float getCrossSectionArea() {
		return crossSectionArea;
	}

	public void setCrossSectionArea(float crossSectionArea) {
		this.crossSectionArea = crossSectionArea;
	}

}
