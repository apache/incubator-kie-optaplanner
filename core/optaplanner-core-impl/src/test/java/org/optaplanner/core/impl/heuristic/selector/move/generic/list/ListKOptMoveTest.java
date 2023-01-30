package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.ValidableAnswer;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableDemand;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableListener;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.optaplanner.core.impl.testdata.util.PlannerTestUtils.mockRebasingScoreDirector;

class ListKOptMoveTest {
    private final SolutionDescriptor<TestdataListSolution> solutionDescriptor = TestdataListSolution.buildSolutionDescriptor();
    private final InnerScoreDirector<TestdataListSolution, ?> scoreDirector = PlannerTestUtils.mockScoreDirector(solutionDescriptor);
    private final ListVariableDescriptor<TestdataListSolution> variableDescriptor = solutionDescriptor.getListVariableDescriptors().get(0);

    @Test
    void doMove() {
        IndexVariableSupply indexVariableSupply = scoreDirector.getSupplyManager().demand(new IndexVariableDemand<>(variableDescriptor));
        IndexVariableListener indexVariableListener = (IndexVariableListener) indexVariableSupply;
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListValue v6 = new TestdataListValue("6");
        TestdataListValue v7 = new TestdataListValue("7");
        TestdataListValue v8 = new TestdataListValue("8");
        TestdataListEntity e1 = new TestdataListEntity("e1", new ArrayList<>(
                List.of(v1, v2, v4, v3, v8, v7, v5, v6)
        ));

        indexVariableListener.afterListVariableChanged(scoreDirector, e1, 0, 8);

        Function<Object, Object> SUC = a -> e1.getValueList().get((indexVariableSupply.getIndex(a) + 1) % e1.getValueList().size());
        TriPredicate<Object, Object, Object> BETWEEN = (a, b, c) -> {
            int aIndex = indexVariableSupply.getIndex(a);
            int bIndex = indexVariableSupply.getIndex(b);
            int cIndex = indexVariableSupply.getIndex(c);

            if (aIndex <= cIndex) {
                return aIndex <= bIndex && bIndex <= cIndex;
            } else {
                return bIndex >= aIndex || bIndex <= cIndex;
            }
        };

        // 4-Opt((v2, v5), (v3, v6))
        /*TestdataListValue[] values = new TestdataListValue[]{v1, v2, v3, v4, v5, v6, v7, v8};
        PermIterator outerIterator = new PermIterator(values.length);
        while (outerIterator.hasNext()){
            int[] outerPerm = outerIterator.next();
            for (int i = 0; i < outerPerm.length; i++) {
                e1.getValueList().set(i, values[outerPerm[i]]);
            }
            indexVariableListener.afterListVariableChanged(scoreDirector, e1, 0, 8);
            PermIterator iterator = new PermIterator(values.length);
            while (iterator.hasNext()) {
                int[] perm = iterator.next();
                Object[] t = new Object[]{
                        null, values[perm[0]], values[perm[1]], values[perm[2]], values[perm[3]],
                        values[perm[4]], values[perm[5]], values[perm[6]], values[perm[7]]
                };
                KOptDescriptor<TestdataListSolution> descriptor = new KOptDescriptor<>(t,
                                                                                       SUC,
                                                                                       BETWEEN);

                if (Arrays.equals(new Integer[]{null, 1, 2, 4, 3, 8, 7, 5, 6},
                                  descriptor.getPermutation())) {
                    System.out.println("================");
                    System.out.println(e1.getValueList());
                    System.out.println(Arrays.toString(t));
                    System.out.println("================");
                }
            }
        }*/

        Object[] t = new Object[] {
                null, v2, v3, v4, v5, v1, v6, v7, v8
        };



        KOptDescriptor<TestdataListSolution> descriptor = new KOptDescriptor<>(t,
                                                                               SUC,
                                                                               BETWEEN);
        System.out.println(descriptor.getMoveInfo());
    }

    private static class PermIterator
            implements Iterator<int[]> {
        private int[] next = null;

        private final int n;
        private int[] perm;
        private int[] dirs;

        public PermIterator(int size) {
            n = size;
            if (n <= 0) {
                perm = (dirs = null);
            } else {
                perm = new int[n];
                dirs = new int[n];
                for(int i = 0; i < n; i++) {
                    perm[i] = i;
                    dirs[i] = -1;
                }
                dirs[0] = 0;
            }

            next = perm;
        }

        @Override
        public int[] next() {
            int[] r = makeNext();
            next = null;
            return r;
        }

        @Override
        public boolean hasNext() {
            return (makeNext() != null);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int[] makeNext() {
            if (next != null)
                return next;
            if (perm == null)
                return null;

            // find the largest element with != 0 direction
            int i = -1, e = -1;
            for(int j = 0; j < n; j++)
                if ((dirs[j] != 0) && (perm[j] > e)) {
                    e = perm[j];
                    i = j;
                }

            if (i == -1) // no such element -> no more premutations
                return (next = (perm = (dirs = null))); // no more permutations

            // swap with the element in its direction
            int k = i + dirs[i];
            swap(i, k, dirs);
            swap(i, k, perm);
            // if it's at the start/end or the next element in the direction
            // is greater, reset its direction.
            if ((k == 0) || (k == n-1) || (perm[k + dirs[k]] > e))
                dirs[k] = 0;

            // set directions to all greater elements
            for(int j = 0; j < n; j++)
                if (perm[j] > e)
                    dirs[j] = (j < k) ? +1 : -1;

            return (next = perm);
        }

        protected static void swap(int i, int j, int[] arr) {
            int v = arr[i];
            arr[i] = arr[j];
            arr[j] = v;
        }
    }
}
