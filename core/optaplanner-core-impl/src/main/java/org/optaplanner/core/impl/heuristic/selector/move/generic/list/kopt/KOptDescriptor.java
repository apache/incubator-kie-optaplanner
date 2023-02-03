package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.util.Pair;

public final class KOptDescriptor<Solution_> {

    /**
     * The number of edges being added
     */
    private final int k;

    /**
     * A sequence of 2K nodes (t_0, t_1, ..., t_2K) where:
     * (t_(2i - 1), t_(2i)) is an out-edge (an edge to be deleted)
     * (t_(2i), t_(2i+1)) is an in-edge (an edge to be added)
     */
    private final Object[] t;

    /**
     * The order each node is visited when the tour is travelled in the successor direction
     */
    private final Integer[] p;

    /**
     * The order each node is visited when the tour is travelled in the successor direction. It the
     * inverse of {@link KOptDescriptor#p} (i.e.
     * {@link KOptDescriptor#p}[predecessorVisitingOrder[i]] == i
     *
     */
    private final Integer[] q;

    private final Integer[] incl;

    private static Integer[] computeInEdges(Object[] t) {
        Integer[] out = new Integer[t.length];
        int k = (t.length - 1) >> 1;

        out[1] = t.length - 1;
        out[t.length - 1] = 1;
        for (int i = 1; i < k; i++) {
            out[2 * i + 1] = 2 * i;
            out[2 * i] = 2 * i + 1;
        }

        return out;
    }

    public KOptDescriptor(
            Object[] t,
            Function<Object, Object> SUC,
            TriPredicate<Object, Object, Object> BETWEEN) {
        this(t, computeInEdges(t), SUC, BETWEEN);
    }

    public KOptDescriptor(
            Object[] t,
            Integer[] incl,
            Function<Object, Object> SUC,
            TriPredicate<Object, Object, Object> BETWEEN) {
        int i, j;
        this.k = (t.length - 1) >> 1;
        this.t = t;
        this.p = new Integer[t.length];
        this.q = new Integer[t.length];
        this.incl = incl;

        for (i = j = 1; j <= k; i += 2, j++) {
            p[j] = (SUC.apply(t[i]) == t[i + 1]) ? i : i + 1;
        }

        Comparator<Integer> Compare = (pa, pb) -> pa.equals(pb) ? 0 : (BETWEEN.test(t[p[1]], t[pa], t[pb]) ? -1 : 1);
        Arrays.sort(p, 2, k + 1, Compare);

        for (j = 2 * k; j >= 2; j -= 2) {
            p[j - 1] = i = p[j / 2];
            p[j] = ((i & 1) == 1) ? i + 1 : i - 1;
        }

        for (i = 1; i <= 2 * k; i++) {
            q[p[i]] = i;
        }
    }

    public Integer[] getPermutation() {
        return p;
    }

    /**
     * Breaks the permutation p into its disjoint cycles
     */
    public KOptCycleInfo Cycles() {
        int cycleCount = 0;
        Integer[] indexToCycle = new Integer[p.length];
        Set<Integer> remaining = IntStream.range(1, p.length).boxed().collect(Collectors.toSet());
        while (!remaining.isEmpty()) {
            Integer current = remaining.iterator().next();
            remaining.remove(current);

            while (true) {
                indexToCycle[current] = cycleCount;
                current = q[incl[p[current]]];
                if (!remaining.contains(current)) {
                    break;
                }
                remaining.remove(current);
            }
            cycleCount++;
        }

        return new KOptCycleInfo(cycleCount, indexToCycle);
    }

    public List<Pair<Object, Object>> getInEdges() {
        List<Pair<Object, Object>> out = new ArrayList<>(2 * k);
        for (int i = 1; i <= k; i++) {
            out.add(Pair.of(t[2 * i], t[(2 * i + 1) % (2 * k)]));
        }
        return out;
    }

    public List<Pair<Object, Object>> getOutEdges() {
        List<Pair<Object, Object>> out = new ArrayList<>(2 * k);
        for (int i = 1; i <= k; i++) {
            out.add(Pair.of(t[2 * i - 1], t[2 * i]));
        }
        return out;
    }

    public KOptListMove<Solution_> getKOptListMove(ListVariableDescriptor listVariableDescriptor,
            IndexVariableSupply indexVariableSupply,
            Object entity) {
        if (!isFeasible()) {
            return new KOptListMove<>(listVariableDescriptor, entity, k, List.of(), 0);
        }

        int i, j, Best_i, Best_j, BestScore, s;
        int entityListSize = listVariableDescriptor.getListSize(entity);
        List<FlipSublistMove<Solution_>> out = new ArrayList<>();
        List<Integer> originalToCurrentIndexList = new ArrayList<>(entityListSize);
        for (int index = 0; index < entityListSize; index++) {
            originalToCurrentIndexList.add(index);
        }

        boolean isMoveNotDone = true;
        Best_i = -1;
        Best_j = -1;
        FindNextReversal: while (isMoveNotDone) {
            BestScore = -1;
            for (i = 1; i <= 2 * k - 2; i++) {
                j = q[incl[p[i]]];
                if (j >= i + 2 && (i & 1) == (j & 1) &&
                        (s = (i & 1) == 1 ? Score(i + 1, j, k) : Score(i, j - 1, k)) > BestScore) {
                    BestScore = s;
                    Best_i = i;
                    Best_j = j;
                }
            }
            if (BestScore >= 0) {
                i = Best_i;
                j = Best_j;
                if ((i & 1) == 1) {
                    out.add(FLIP(listVariableDescriptor, indexVariableSupply, originalToCurrentIndexList, entity,
                            t[p[i + 1]], t[p[i]], t[p[j]], t[p[j + 1]]));
                    Reverse(i + 1, j);
                } else {
                    out.add(FLIP(listVariableDescriptor, indexVariableSupply, originalToCurrentIndexList, entity,
                            t[p[i - 1]], t[p[i]], t[p[j]], t[p[j - 1]]));
                    Reverse(i, j - 1);
                }
                continue FindNextReversal;
            }
            for (i = 1; i <= 2 * k - 1; i += 2) {
                j = q[incl[p[i]]];
                if (j >= i + 2) {
                    out.add(FLIP(listVariableDescriptor, indexVariableSupply, originalToCurrentIndexList, entity,
                            t[p[i]], t[p[i + 1]], t[p[j]], t[p[j - 1]]));
                    Reverse(i + 1, j - 1);
                    continue FindNextReversal;
                }
            }
            isMoveNotDone = false;
        }
        return new KOptListMove<>(listVariableDescriptor, entity, k, out, -originalToCurrentIndexList.indexOf(0));
    }

    public boolean isFeasible() {
        int Count = 1, i = 2 * k;
        while ((i = q[incl[p[i]]] ^ 1) != 0) {
            Count++;
        }
        return (Count == k);
    }

    private void Reverse(int i, int j) {
        for (; i < j; i++, j--) {
            int pi = p[i];
            q[p[i] = p[j]] = i;
            q[p[j] = pi] = j;
        }
    }

    private int Score(int left, int right, int k) {
        int count = 0, i, j;
        Reverse(left, right);
        for (i = 1; i <= 2 * k - 2; i++) {
            j = q[incl[p[i]]];
            if (j >= i + 2 && (i & 1) == (j & 1)) {
                count++;
            }
        }
        Reverse(left, right);
        return count;
    }

    private FlipSublistMove<Solution_> FLIP(ListVariableDescriptor<Solution_> listVariableDescriptor,
            IndexVariableSupply indexVariableSupply,
            List<Integer> originalToCurrentIndexList,
            Object entity,
            Object firstEdgeStart,
            Object firstEdgeEnd,
            Object secondEdgeStart,
            Object secondEdgeEnd) {
        int originalFirstEdgeStartIndex = originalToCurrentIndexList.indexOf(indexVariableSupply.getIndex(firstEdgeStart));
        int originalFirstEdgeEndIndex = originalToCurrentIndexList.indexOf(indexVariableSupply.getIndex(firstEdgeEnd));
        int originalSecondEdgeStartIndex = originalToCurrentIndexList.indexOf(indexVariableSupply.getIndex(secondEdgeStart));
        int originalSecondEdgeEndIndex = originalToCurrentIndexList.indexOf(indexVariableSupply.getIndex(secondEdgeEnd));

        int firstEndpoint = ((originalFirstEdgeStartIndex + 1) % originalToCurrentIndexList.size()) == originalFirstEdgeEndIndex
                ? originalFirstEdgeEndIndex
                : originalFirstEdgeStartIndex;

        int secondEndpoint =
                ((originalSecondEdgeStartIndex + 1) % originalToCurrentIndexList.size()) == originalSecondEdgeEndIndex
                        ? originalSecondEdgeEndIndex
                        : originalSecondEdgeStartIndex;

        FlipSublistMove.flipSublist(originalToCurrentIndexList, firstEndpoint, secondEndpoint);
        return new FlipSublistMove<>(listVariableDescriptor, entity,
                firstEndpoint, secondEndpoint);
    }

    public int getK() {
        return k;
    }

    public String getMoveInfo() {
        StringBuilder out = new StringBuilder();
        out.append("k =    ").append(k).append("\n");

        out.append("t =    [");
        for (int i = 1; i < t.length; i++) {
            out.append(t[i]).append(", ");
        }
        out.delete(out.length() - 2, out.length()).append("]\n");

        out.append("p =    [");
        for (int i = 1; i < p.length; i++) {
            out.append(p[i]).append(", ");
        }
        out.delete(out.length() - 2, out.length()).append("]\n");

        out.append("q =    [");
        for (int i = 1; i < q.length; i++) {
            out.append(q[i]).append(", ");
        }
        out.delete(out.length() - 2, out.length()).append("]\n");

        out.append("incl = [");
        for (int i = 1; i < incl.length; i++) {
            out.append(incl[i]).append(", ");
        }
        out.delete(out.length() - 2, out.length()).append("]\n");

        out.append("cycles = ").append(Cycles()).append("\n");

        return out.toString();
    }

    public String toString() {
        return k + "-opt(t=" + Arrays.toString(t) + "\nout-edges=" + getOutEdges() + "\n, in-edges=" + getInEdges() + ")";
    }
}
