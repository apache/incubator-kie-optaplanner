package org.optaplanner.core.impl.domain.variable.custom;

import org.junit.Test;

public class CustomShadowVariableTest {

    @Test
    public void customShadowVariable() {
//        Solution problem = Examples.chainedSorting().loadSolvingProblem(ChainedSorting.DataSet.CS_single_chain);
//        Solver solver = OptaPlannerUtils.runSolver(Examples.chainedSorting().getDefaultConfig().buildSolver(), problem);
//        ChainedSortingSolution solution = (ChainedSortingSolution) solver.getBestSolution();
//        List<StartingPoint> startingPoints = solution.getStartingPointList();
//        for (int i = 0; i < startingPoints.size(); i++) {
//            NumberIface start = startingPoints.get(i);
//            for (; start != null; ) {
//                /**
//                 * since startingPoint's value == 0, in each entity should be forward sum == it's value;
//                 */
//                Assertions.assertThat(start.getValue()).as("ForwardSum is not equal to actual value.")
//                        .isEqualTo(start.getForwardSum());
//                logger.debug("Chain {} : {}", i, start.getValue());
//                start = start.getNextNumber();
//            }
//            logger.debug("--------------------------------------------------------------------------------");
//        }
    }

}
