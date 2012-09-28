package org.drools.planner.core.constructionheuristic.greedyFit.decider;

import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.junit.Test;

public class ConstructionHeuristicTest {

	/** The Constant SOLVER_CONFIG. */
	public static final String SOLVER_CONFIG = "/org/drools/planner/core/constructionheuristic/greedyFit/decider/fooBarSolverConfig.xml";

	@Test
	public void testConstructionHeuristic() {
		FooBarSolution planningProblem = new FooBarSolution();
		
		planningProblem.getFooList().add(new Foo());
		planningProblem.getBarList().add(new Bar());
		
		XmlSolverFactory solverFactory = new XmlSolverFactory();
		solverFactory.configure(SOLVER_CONFIG);
		Solver solver = solverFactory.buildSolver();

		solver.setPlanningProblem(planningProblem);
		solver.solve();

	}
}
