package org.optaplanner.examples.batchscheduling.domain;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@XStreamAlias("PipeBatch")
public class Batch extends AbstractPersistable {

    private List<RoutePath> routePathList;
    private String name;
	private Double volume;
	private Long delayRangeValue; 

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public List<RoutePath> getRoutePathList() {
        return routePathList;
    }

    public void setRoutePathList(List<RoutePath> routePathList) {
        this.routePathList = routePathList;
    }


	public Long getDelayRangeValue() {
		return delayRangeValue;
	}

	public void setDelayRangeValue(Long delayRangeValue) {
		this.delayRangeValue = delayRangeValue;
	}

}
