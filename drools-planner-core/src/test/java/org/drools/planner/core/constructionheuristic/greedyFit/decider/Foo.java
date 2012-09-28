package org.drools.planner.core.constructionheuristic.greedyFit.decider;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.api.domain.variable.ValueRangeType;

@PlanningEntity
public class Foo implements Cloneable {
	Bar bar;
	
    @PlanningVariable
    @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty="barList")
	public Bar getBar() {
		return bar;
	}
	
	public void setBar(Bar bar) {
		this.bar = bar;
	}
	
	public Foo clone() {
		final Foo cloneFoo = new Foo();
		cloneFoo.setBar(this.bar);
		return cloneFoo;
	}
}
