package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

public class ListKOptMoveIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    private final Random workingRandom;
    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final SingletonInverseVariableSupply inverseVariableSupply;
    private final IndexVariableSupply indexVariableSupply;
    private final EntitySelector<Solution_> entitySelector;
    private final ValueSelector<Solution_> valueSelector;
    private final int minK;
    private final int maxK;
    private final int Patching_C;

    private Iterator<Object> entityIterator;

    public ListKOptMoveIterator(Random workingRandom,
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply,
            EntitySelector<Solution_> entitySelector,
            ValueSelector<Solution_> valueSelector,
            int minK,
            int maxK) {
        this.workingRandom = workingRandom;
        this.listVariableDescriptor = listVariableDescriptor;
        this.inverseVariableSupply = inverseVariableSupply;
        this.indexVariableSupply = indexVariableSupply;
        this.entitySelector = entitySelector;
        this.valueSelector = valueSelector;
        this.minK = minK;
        this.maxK = maxK;
        this.Patching_C = maxK;
    }

    private Iterator<Object> getValueIteratorForEntity(Object entity) {
        // TODO: Find out why valueSelector.iterator(entity) return values with different entities
        List<Object> entityListVariable = listVariableDescriptor.getListVariable(entity);
        return workingRandom.ints(0, entityListVariable.size())
                .mapToObj(entityListVariable::get).iterator();
    }

    @Override
    protected Move<Solution_> createUpcomingSelection() {
        int k = (minK == maxK)? minK : workingRandom.nextInt(maxK - minK) + minK;
        Object entity = pickEntityWithMinimumRouteLength(k);
        while (entity == null) {
            k--;
            if (k <= 1) {
                // Was unable to find an entity with more than 1 value in its route
                // (rare, but possible)
                return new ListKOptMove<>(listVariableDescriptor, k, List.of());
            }
            entity = pickEntityWithMinimumRouteLength(k);
        }
        if (k == 2) {
            Iterator<Object> valueIterator = getValueIteratorForEntity(entity);
            Object firstEndpoint = valueIterator.next();
            Object secondEndpoint = valueIterator.next();
            while (secondEndpoint == firstEndpoint) {
                secondEndpoint = valueIterator.next();
            }
            return new List2OptMove<>(listVariableDescriptor, indexVariableSupply, entity, firstEndpoint,
                                      secondEndpoint);
        }
        KOptDescriptor<Solution_> descriptor = pickKOptMove(entity, k);
        if (descriptor == null) {
            // Was unable to find a K-Opt move
            return new ListKOptMove<>(listVariableDescriptor, k, List.of());
        }
        return new ListKOptMove<>(listVariableDescriptor, k, descriptor.as2OptMoves(listVariableDescriptor, indexVariableSupply, entity));
    }

    private Object pickEntityWithMinimumRouteLength(int minimumLength) {
        if (entityIterator == null) {
            entityIterator = entitySelector.endingIterator();
        }
        for (int i = 0; i < 2; i++) {
            while (entityIterator.hasNext()) {
                Object entity = entityIterator.next();
                if (listVariableDescriptor.getListSize(entity) >= minimumLength) {
                    return entity;
                }
            }
            entityIterator = entitySelector.endingIterator();
        }
        return null;
    }

    private KOptDescriptor<Solution_>  pickKOptMove(Object entity, int k) {
        // The code in the paper used 1-index arrays
        Object[] pickedValues = new Object[2 * k + 1];
        Iterator<Object> valueIterator = getValueIteratorForEntity(entity);
        pickedValues[1] = valueIterator.next();
        pickedValues[2] = workingRandom.nextBoolean() ? PRED(pickedValues[1]) : SUC(pickedValues[1]);
        return pickKOptMoveRec(valueIterator, pickedValues, 2, k);
    }

    private KOptDescriptor<Solution_> pickKOptMoveRec(Iterator<Object> valueIterator, Object[] pickedValues, int pickedSoFar, int K) {
        Object t2 = pickedValues[2 * pickedSoFar - 2];
        Object t3, t4;

        for (int i = 0; i < listVariableDescriptor.getListSize(inverseVariableSupply.getInverseSingleton(pickedValues[1])); i++) {
            t3 = valueIterator.next();
            while (t3 == PRED(t2) || t3 == SUC(t2) || Added(pickedValues, t2, t3, pickedSoFar - 2) ||
                   (Deleted(pickedValues, t3, PRED(t3), pickedSoFar - 2) && Deleted(pickedValues, t3, SUC(t3), pickedSoFar - 2))){
                t3 = valueIterator.next();
            }

            pickedValues[2 * pickedSoFar - 1] = t3;
            if (Deleted(pickedValues, t3, PRED(t3), pickedSoFar - 2)) {
                t4 = SUC(t3);
            } else if (Deleted(pickedValues, t3, SUC(t3), pickedSoFar - 2)) {
                t4 = PRED(t3);
            } else {
                t4 = workingRandom.nextBoolean()? SUC(t3) : PRED(t3);
            }
            pickedValues[2 * pickedSoFar] = t4;

            if (pickedSoFar < K) {
                KOptDescriptor<Solution_> descriptor = pickKOptMoveRec(valueIterator, pickedValues, pickedSoFar + 1, K);
                if (descriptor != null) {
                    return descriptor;
                }
            } else {
                KOptDescriptor<Solution_> descriptor = new KOptDescriptor<>(pickedValues, this::SUC, this::BETWEEN);
                if (descriptor.isFeasible()) {
                    return descriptor;
                } else {
                    descriptor = PatchCycles(valueIterator, descriptor, pickedValues, pickedSoFar);
                    if (descriptor.isFeasible()) {
                        return descriptor;
                    }
                }
            }
        }
        return null;
    }

    KOptDescriptor<Solution_> PatchCycles(Iterator<Object> valueIterator,
                                          KOptDescriptor<Solution_> descriptor, Object[] oldT, int k) {
        Object s1, s2, sStart, sStop;
        int M, i;
        Integer[] p = descriptor.getPermutation();
        KOptCycleInfo cycleInfo = descriptor.Cycles();
        M = cycleInfo.cycleCount;
        Integer[] cycle = cycleInfo.indexToCycle;

        if (M == 1 || M > Patching_C) {
            return descriptor;
        }
        int CurrentCycle = ShortestCycle(oldT, cycle, p, M, k);
        for (i = 0; i < k; i++) {
            if (cycle[p[2 * i]] == CurrentCycle) {
                sStart = oldT[p[2 * i]];
                sStop = oldT[p[2 * i + 1]];
                for (s1 = sStart; s1 != sStop; s1 = s2) {
                    Object[] t = Arrays.copyOf(oldT, oldT.length + 2);

                    t[2 * k + 1] = s1;
                    t[2 * k + 2] = s2 = SUC(s1);
                    Integer[] incl = new Integer[t.length];
                    KOptDescriptor<Solution_> newMove = PatchCyclesRec(valueIterator, descriptor, t, incl, cycle, CurrentCycle,
                                                                       k, 2, M);
                    if (newMove.isFeasible()) {
                        return newMove;
                    }
                }
            }
        }
        return descriptor;
    }

    KOptDescriptor<Solution_> PatchCyclesRec(Iterator<Object> valueIterator, KOptDescriptor<Solution_> originalMove,
                                             Object[] oldT, Integer[] incl, Integer[] cycle, int CurrentCycle,
                                             int k, int m, int M) {
        Object s1, s2, s3, s4;
        int NewCycle, i;
        Integer[] cycleSaved = new Integer[1 + 2 * k];
        Object[] t = Arrays.copyOf(oldT, oldT.length + 2);

        s1 = t[2 * k + 1];
        s2 = t[i = 2 * (k + m) - 2];
        incl[incl[i] = i + 1] = i;
        for (i = 1; i <= 2 * k; i++) {
            cycleSaved[i] = cycle[i];
        }

        s3 = valueIterator.next();
        while (s3 != PRED(s2) || s3 != SUC(s2) || ((NewCycle = FindCycle(s3, t, originalMove.getPermutation(), cycle)) == CurrentCycle) ||
                (Deleted(t, s3, PRED(s3), k) && Deleted(t, s3, SUC(s3), k))) {
            s3 = valueIterator.next();
        }
        t[2 * (k + m) - 1] = s3;
        if (Deleted(t, s3, PRED(s3), k)) {
            s4 = SUC(s3);
        } else if (Deleted(t, s3, SUC(s3), k)) {
            s4 = PRED(s3);
        } else {
            s4 = workingRandom.nextBoolean()? SUC(s3) : PRED(s3);
        }
        t[2 * (k + m)] = s4;
        if (M > 2) {
            for (i = 1; i <= 2 * k; i++) {
                if (cycle[i] == NewCycle) {
                    cycle[i] = CurrentCycle;
                }
            }
            KOptDescriptor<Solution_> recursiveCall = PatchCyclesRec(valueIterator, originalMove, t, incl, cycle, CurrentCycle,
                                                                     k, m + 1, M - 1);
            if (recursiveCall.isFeasible()) {
                return recursiveCall;
            }
            for (i = 1; i <= 2 * k; i++) {
                cycle[i]= cycleSaved[i];
            }
        } else if (s4 != s1) {
            incl[incl[2 * k + 1] = 2 * (k + m)] =
                    2 * k + 1;
            return new KOptDescriptor<>(t, incl, this::SUC, this::BETWEEN);
        }
        return originalMove;
    }

    int FindCycle(Object value, Object[] pickedValues, Integer[] permutation, Integer[] indexToCycle) {
        for (int i = 1; i < pickedValues.length; i++) {
            if (BETWEEN(pickedValues[permutation[i]], value, pickedValues[permutation[i+1]])) {
                return indexToCycle[permutation[i]];
            }
        }
        throw new IllegalStateException("Cannot find cycle the " + value + " belongs to");
    }

    int ShortestCycle(Object[] t, Integer[] cycle, Integer[] p, int M, int k) {
        int i;
        int MinCycle = 0;
        int MinSize = Integer.MAX_VALUE;
        int[] size = new int[M + 1];

        for (i = 1; i <= M; i++) {
            size[i] = 0;
        }
        p[0] = p[2 * k];
        for (i = 0; i < 2 * k; i += 2) {
            size[cycle[p[i]]] +=
                    SegmentSize(t[p[i]], t[p[i + 1]]);
        }
        for (i = 1; i <= M; i++) {
            if (size[i] < MinSize) {
                MinSize = size[i];
                MinCycle = i;
            }
        }
        return MinCycle;
    }

    private int SegmentSize(Object from, Object to) {
        int startIndex = indexVariableSupply.getIndex(from);
        int endIndex = indexVariableSupply.getIndex(to);

        if (startIndex <= endIndex) {
            return endIndex - startIndex;
        } else {
            return listVariableDescriptor.getListSize(inverseVariableSupply.getInverseSingleton(from)) - startIndex + endIndex;
        }
    }

    private boolean Added(Object[] pickedValues, Object ta, Object tb, int k) {
        int i = 2 * k;
        while ((i -= 2) > 0) {
            if ((ta == pickedValues[i] && tb == pickedValues[i + 1]) ||
                    (ta == pickedValues[i + 1] && tb == pickedValues[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean Deleted(Object[] pickedValues, Object ta, Object tb, int k) {
        int i = 2 * k + 2;
        while ((i -= 2) > 0) {
            if ((ta == pickedValues[i - 1] && tb == pickedValues[i]) ||
                    (ta == pickedValues[i] && tb == pickedValues[i - 1])) {
                return true;
            }
        }
        return false;
    }

    private Object SUC(Object node) {
        List<Object> valueList = listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
        int index = indexVariableSupply.getIndex(node);
        if (index == valueList.size() - 1) {
            return valueList.get(0);
        } else {
            return valueList.get(index + 1);
        }
    }

    private Object PRED(Object node) {
        List<Object> valueList = listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
        int index = indexVariableSupply.getIndex(node);
        if (index == valueList.size() - 1) {
            return valueList.get(0);
        } else {
            return valueList.get(index + 1);
        }
    }

    private boolean BETWEEN(Object start, Object middle, Object end) {
        int startIndex = indexVariableSupply.getIndex(start);
        int middleIndex = indexVariableSupply.getIndex(middle);
        int endIndex = indexVariableSupply.getIndex(end);

        if (startIndex <= endIndex) {
            // test middleIndex in [startIndex, endIndex]
            return startIndex <= middleIndex && middleIndex <= endIndex;
        } else {
            // test middleIndex in [0, endIndex] or middleIndex in [startIndex, listSize)
            return middleIndex >= startIndex || middleIndex <= endIndex;
        }
    }
}
