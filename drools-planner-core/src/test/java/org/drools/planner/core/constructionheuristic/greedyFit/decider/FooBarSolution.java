package org.drools.planner.core.constructionheuristic.greedyFit.decider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.core.solution.Solution;

public class FooBarSolution implements Solution<HardAndSoftScore> {

	private List<Foo> fooList = new ArrayList<Foo>();
	private List<Bar> barList = new ArrayList<Bar>();

	private transient HardAndSoftScore score;

	@PlanningEntityCollectionProperty
	public List<Foo> getFooList() {
		return fooList;
	}

	public void setFooList(List<Foo> fooList) {
		this.fooList = fooList;
	}

	public List<Bar> getBarList() {
		return barList;
	}

	public void setBarList(List<Bar> barList) {
		this.barList = barList;
	}

	public HardAndSoftScore getScore() {
		return score;
	}

	public void setScore(HardAndSoftScore score) {
		this.score = score;
	}

	public Collection<? extends Object> getProblemFacts() {
		List<Object> facts = new ArrayList<Object>();
		if (barList != null) {
			facts.addAll(barList);
		}

		return facts;
	}

	/**
	 * Clone will only deep copy the {@link #assignmentList} and {@link #resourceList}.
	 */
	public FooBarSolution cloneSolution() {
		FooBarSolution clone = new FooBarSolution();
		clone.score = score;
		clone.barList = barList;

		if (fooList != null) {
			clone.fooList = new ArrayList<Foo>(fooList.size());
			for (Foo foo : fooList) {
				clone.fooList.add(foo.clone());
			}
		}

		return clone;
	}
}
