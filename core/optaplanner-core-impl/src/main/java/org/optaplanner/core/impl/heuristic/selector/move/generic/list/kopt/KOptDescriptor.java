package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.ArrayList;
import java.util.Arrays;
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
     * A sequence of 2K nodes that forms the sequence of edges being removed
     */
    private final Object[] removedEdges;

    /**
     * The order each node is visited when the tour is travelled in the successor direction. This forms
     * a 2K-cycle representing the permutation performed by the K-opt move.
     */
    private final int[] removedEdgeIndexToTourOrder;

    /**
     * The order each node is visited when the tour is travelled in the predecessor direction. It the
     * inverse of {@link KOptDescriptor#removedEdgeIndexToTourOrder} (i.e.
     * {@link KOptDescriptor#removedEdgeIndexToTourOrder}[inverseRemovedEdgeIndexToTourOrder[i]] == i
     *
     */
    private final int[] inverseRemovedEdgeIndexToTourOrder;

    /**
     * Maps the index of a removed edge endpoint to its corresponding added edge other endpoint. For instance,
     * if the removed edges are (a, b), (c, d), (e, f) and the added edges are (a, d), (c, f), (e, b), then
     * <br />
     * removedEdges = [null, a, b, c, d, e, f] <br />
     * addedEdgeToOtherEndpoint = [null, 4, 5, 6, 1, 2, 3] <br />
     * <br />
     * For any valid removedEdges index, (removedEdges[index], removedEdges[addedEdgeToOtherEndpoint[index]])
     * is an edge added by this K-Opt move.
     */
    private final int[] addedEdgeToOtherEndpoint;

    private static int[] computeInEdgesForSequentialMove(Object[] removedEdges) {
        int[] out = new int[removedEdges.length];
        int k = (removedEdges.length - 1) >> 1;

        out[1] = removedEdges.length - 1;
        out[removedEdges.length - 1] = 1;
        for (int i = 1; i < k; i++) {
            out[2 * i + 1] = 2 * i;
            out[2 * i] = 2 * i + 1;
        }

        return out;
    }

    /**
     * Create a sequential {@link KOptDescriptor} from the given removed edges.
     *
     * @param removedEdges The edges removed from the tour. The added edges will
     *        be formed from opposite endpoints for consecutive edges.
     * @param endpointToSuccessorFunction A {@link Function} that maps an endpoint to its successor
     * @param betweenPredicate A {@link TriPredicate} that return true if and only if its middle
     *        argument is between its first and last argument when the tour is
     *        taken in the successor direction.
     */
    public KOptDescriptor(
            Object[] removedEdges,
            Function<Object, Object> endpointToSuccessorFunction,
            TriPredicate<Object, Object, Object> betweenPredicate) {
        this(removedEdges, computeInEdgesForSequentialMove(removedEdges), endpointToSuccessorFunction, betweenPredicate);
    }

    /**
     * Create as sequential or non-sequential {@link KOptDescriptor} from the given removed
     * and added edges.
     *
     * @param removedEdges The edges removed from the tour.
     * @param addedEdgeToOtherEndpoint The edges added to the tour.
     * @param endpointToSuccessorFunction A {@link Function} that maps an endpoint to its successor
     * @param betweenPredicate A {@link TriPredicate} that return true if and only if its middle
     *        argument is between its first and last argument when the tour is
     *        taken in the successor direction.
     */
    public KOptDescriptor(
            Object[] removedEdges,
            int[] addedEdgeToOtherEndpoint,
            Function<Object, Object> endpointToSuccessorFunction,
            TriPredicate<Object, Object, Object> betweenPredicate) {
        int i, j;
        this.k = (removedEdges.length - 1) >> 1;
        this.removedEdges = removedEdges;
        this.removedEdgeIndexToTourOrder = new int[removedEdges.length];
        this.inverseRemovedEdgeIndexToTourOrder = new int[removedEdges.length];
        this.addedEdgeToOtherEndpoint = addedEdgeToOtherEndpoint;

        // Compute the permutation as described in FindPermutation
        // (Section 5.3 "Determination of the feasibility of a move",
        //  An Effective Implementation of K-opt Moves for the Lin-Kernighan TSP Heuristic)
        for (i = j = 1; j <= k; i += 2, j++) {
            removedEdgeIndexToTourOrder[j] = (endpointToSuccessorFunction.apply(removedEdges[i]) == removedEdges[i + 1]) ? i : i + 1;
        }

        IntComparator comparator = (pa, pb) -> pa == pb ? 0
                : (betweenPredicate.test(removedEdges[removedEdgeIndexToTourOrder[1]], removedEdges[pa], removedEdges[pb]) ? -1 : 1);
        TimSort.sort(removedEdgeIndexToTourOrder, 2, k + 1, comparator);

        for (j = 2 * k; j >= 2; j -= 2) {
            removedEdgeIndexToTourOrder[j - 1] = i = removedEdgeIndexToTourOrder[j / 2];
            removedEdgeIndexToTourOrder[j] = ((i & 1) == 1) ? i + 1 : i - 1;
        }

        for (i = 1; i <= 2 * k; i++) {
            inverseRemovedEdgeIndexToTourOrder[removedEdgeIndexToTourOrder[i]] = i;
        }
    }

    public int[] getRemovedEdgeIndexToTourOrder() {
        return removedEdgeIndexToTourOrder;
    }

    /**
     * Calculate the disjoint k-cycles for {@link KOptDescriptor#removedEdgeIndexToTourOrder}. <br />
     * <br />
     * Any permutation can be expressed as combination of k-cycles. A k-cycle is a sequence of
     * unique elements (p_1, p_2, ..., p_k) where
     * <ul>
     * <li>p_1 maps to p_2 in the permutation</li>
     * <li>p_2 maps to p_3 in the permutation</li>
     * <li>p_(k-1) maps to p_k in the permutation</li>
     * <li>p_k maps to p_1 in the permutation</li>
     * <li>In general: p_i maps to p_(i+1) in the permutation</li>
     * </ul>
     * For instance, the permutation
     * <ul>
     * <li>1 -> 2</li>
     * <li>2 -> 3</li>
     * <li>3 -> 1</li>
     * <li>4 -> 5</li>
     * <li>5 -> 4</li>
     * </ul>
     * can be expressed as `(1, 2, 3)(4, 5)`.
     *
     * @return The {@link KOptCycleInfo} corresponding to the permutation described by
     *         {@link KOptDescriptor#removedEdgeIndexToTourOrder}.
     */
    public KOptCycleInfo getCyclesForPermutation() {
        int cycleCount = 0;
        int[] indexToCycle = new int[removedEdgeIndexToTourOrder.length];
        Set<Integer> remaining = IntStream.range(1, removedEdgeIndexToTourOrder.length).boxed().collect(Collectors.toSet());
        while (!remaining.isEmpty()) {
            Integer current = remaining.iterator().next();
            remaining.remove(current);

            while (true) {
                indexToCycle[current] = cycleCount;
                current = inverseRemovedEdgeIndexToTourOrder[addedEdgeToOtherEndpoint[removedEdgeIndexToTourOrder[current]]];
                if (!remaining.contains(current)) {
                    break;
                }
                remaining.remove(current);
            }
            cycleCount++;
        }

        return new KOptCycleInfo(cycleCount, indexToCycle);
    }

    public List<Pair<Object, Object>> getAddedEdges() {
        List<Pair<Object, Object>> out = new ArrayList<>(2 * k);
        for (int i = 1; i <= k; i++) {
            out.add(Pair.of(removedEdges[2 * i], removedEdges[(2 * i + 1) % (2 * k)]));
        }
        return out;
    }

    public List<Pair<Object, Object>> getRemovedEdges() {
        List<Pair<Object, Object>> out = new ArrayList<>(2 * k);
        for (int i = 1; i <= k; i++) {
            out.add(Pair.of(removedEdges[2 * i - 1], removedEdges[2 * i]));
        }
        return out;
    }

    /**
     * This return a {@link KOptListMove} that corresponds to this {@link KOptDescriptor}. <br />
     * <br />
     * It implements the algorithm described in the paper
     * <a href="https://dl.acm.org/doi/pdf/10.1145/300515.300516">"Transforming Cabbage into Turnip: Polynomial
     * Algorithm for Sorting Signed Permutations by Reversals"</a> which is used in the paper
     * <a href="http://webhotel4.ruc.dk/~keld/research/LKH/KoptReport.pdf">"An Effective Implementation of K-opt Moves
     * for the Lin-Kernighan TSP Heuristic"</a> (Section 5.4 "Execution of a feasible move") to perform a K-opt move
     * by performing the minimal number of list reversals to transform the current route into the new route after the
     * K-opt. We use it here to calculate the {@link FlipSublistMove} list for the {@link KOptListMove} that is
     * described by this {@link KOptDescriptor}.<br />
     * <br />
     * The algorithm goal is to convert a signed permutation (p_1, p_2, ..., p_(2k)) into the identify permutation
     * (+1, +2, +3, ..., +(2k - 1), +2k). It can be summarized as:
     *
     * <ul>
     * <li>
     * As long as there are oriented pairs, perform the reversal that corresponds to the oriented pair with the
     * maximal score (described in {@link #countOrientedPairsForReversal}).
     * </li>
     * <li>
     * If there are no oriented pairs, Find the pair (p_i, p_j) for which |p_j – p_i| = 1, j >= i + 3,
     * and i is minimal. Then reverse the segment (p_(i+1) ... p_(j-1). This corresponds to a
     * hurdle cutting operation. Normally, this is not enough to guarantee the number of reversals
     * is optimal, but since each hurdle corresponds to a unique cycle, the number of hurdles in
     * the list at any point is at most 1 (and thus, hurdle cutting is the optimal move). (A hurdle
     * is an subsequence [i, p_j, p_(j+1)..., p_(j+k-1) i+k] that can be sorted so
     * [i, p_j, p_(j+1)..., p_(j+k-1), i+k] are all consecutive integers, which does not contain a subsequence
     * with the previous property ([4, 7, 6, 5, 8] is a hurdle, since the subsequence [7, 6, 5] does not
     * contain all the items between 7 and 5 [8, 1, 2, 3, 4]). This create enough enough oriented pairs
     * to completely sort the permutation.
     * </li>
     * <li>
     * When there are no oriented pairs and no hurdles, the algorithm is completed.
     * </li>
     * </ul>
     *
     * @param listVariableDescriptor
     * @param indexVariableSupply
     * @param entity
     * @return
     */
    public KOptListMove<Solution_> getKOptListMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
            IndexVariableSupply indexVariableSupply,
            Object entity) {
        if (!isFeasible()) {
            // A KOptListMove move with an empty flip move list is not feasible, since if executed, it a no-op
            return new KOptListMove<>(listVariableDescriptor, entity, this, List.of(), 0);
        }

        int entityListSize = listVariableDescriptor.getListSize(entity);
        List<FlipSublistMove<Solution_>> out = new ArrayList<>();
        List<Integer> originalToCurrentIndexList = new ArrayList<>(entityListSize);
        for (int index = 0; index < entityListSize; index++) {
            originalToCurrentIndexList.add(index);
        }

        boolean isMoveNotDone = true;
        int bestOrientedPairFirstEndpoint = -1;
        int bestOrientedPairSecondEndpoint = -1;

        // Copy removedEdgeIndexToTourOrder and inverseRemovedEdgeIndexToTourOrder
        // to avoid mutating the original arrays (since this function mutate the arrays
        // into the sorted signed permutation (+1, +2, ...)
        int[] currentRemovedEdgeIndexToTourOrder =
                Arrays.copyOf(removedEdgeIndexToTourOrder, removedEdgeIndexToTourOrder.length);
        int[] currentInverseRemovedEdgeIndexToTourOrder =
                Arrays.copyOf(inverseRemovedEdgeIndexToTourOrder, inverseRemovedEdgeIndexToTourOrder.length);

        FindNextReversal: while (isMoveNotDone) {
            int maximumOrientedPairCountAfterReversal = -1;
            for (int firstEndpoint = 1; firstEndpoint <= 2 * k - 2; firstEndpoint++) {
                int secondEndpoint =
                        currentInverseRemovedEdgeIndexToTourOrder[addedEdgeToOtherEndpoint[currentRemovedEdgeIndexToTourOrder[firstEndpoint]]];
                if (secondEndpoint >= firstEndpoint + 2 && (firstEndpoint & 1) == (secondEndpoint & 1)) {
                    int orientedPairCountAfterReversal = ((firstEndpoint & 1) == 1)
                            ? countOrientedPairsForReversal(currentRemovedEdgeIndexToTourOrder,
                                    currentInverseRemovedEdgeIndexToTourOrder,
                                    firstEndpoint + 1,
                                    secondEndpoint)
                            : countOrientedPairsForReversal(currentRemovedEdgeIndexToTourOrder,
                                    currentInverseRemovedEdgeIndexToTourOrder,
                                    firstEndpoint,
                                    secondEndpoint - 1);
                    if (orientedPairCountAfterReversal > maximumOrientedPairCountAfterReversal) {
                        maximumOrientedPairCountAfterReversal = orientedPairCountAfterReversal;
                        bestOrientedPairFirstEndpoint = firstEndpoint;
                        bestOrientedPairSecondEndpoint = secondEndpoint;
                    }
                }
            }
            if (maximumOrientedPairCountAfterReversal >= 0) {
                int firstEndpoint = bestOrientedPairFirstEndpoint;
                int secondEndpoint = bestOrientedPairSecondEndpoint;
                if ((firstEndpoint & 1) == 1) {
                    out.add(getListReversalMoveForEdgePair(listVariableDescriptor, indexVariableSupply,
                            originalToCurrentIndexList, entity,
                            removedEdges[currentRemovedEdgeIndexToTourOrder[firstEndpoint + 1]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[firstEndpoint]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[secondEndpoint]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[secondEndpoint + 1]]));
                    reversePermutationPart(currentRemovedEdgeIndexToTourOrder,
                            currentInverseRemovedEdgeIndexToTourOrder,
                            firstEndpoint + 1,
                            secondEndpoint);
                } else {
                    out.add(getListReversalMoveForEdgePair(listVariableDescriptor, indexVariableSupply,
                            originalToCurrentIndexList, entity,
                            removedEdges[currentRemovedEdgeIndexToTourOrder[firstEndpoint - 1]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[firstEndpoint]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[secondEndpoint]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[secondEndpoint - 1]]));
                    reversePermutationPart(currentRemovedEdgeIndexToTourOrder,
                            currentInverseRemovedEdgeIndexToTourOrder,
                            firstEndpoint,
                            secondEndpoint - 1);
                }
                continue FindNextReversal;
            }

            // There are no oriented pairs; check for a hurdle
            for (int firstEndpoint = 1; firstEndpoint <= 2 * k - 1; firstEndpoint += 2) {
                int secondEndpoint =
                        currentInverseRemovedEdgeIndexToTourOrder[addedEdgeToOtherEndpoint[currentRemovedEdgeIndexToTourOrder[firstEndpoint]]];
                if (secondEndpoint >= firstEndpoint + 2) {
                    out.add(getListReversalMoveForEdgePair(listVariableDescriptor, indexVariableSupply,
                            originalToCurrentIndexList, entity,
                            removedEdges[currentRemovedEdgeIndexToTourOrder[firstEndpoint]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[firstEndpoint + 1]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[secondEndpoint]],
                            removedEdges[currentRemovedEdgeIndexToTourOrder[secondEndpoint - 1]]));
                    reversePermutationPart(currentRemovedEdgeIndexToTourOrder,
                            currentInverseRemovedEdgeIndexToTourOrder,
                            firstEndpoint + 1, secondEndpoint - 1);
                    continue FindNextReversal;
                }
            }
            isMoveNotDone = false;
        }

        return new KOptListMove<>(listVariableDescriptor, entity, this, out, -originalToCurrentIndexList.indexOf(0));
    }

    /**
     * Return true if and only if performing the K-opt move described by this {@link KOptDescriptor} will result in a
     * single cycle.
     *
     * @return true if and only if performing the K-opt move described by this {@link KOptDescriptor} will result in a
     *         single cycle, false otherwise.
     */
    public boolean isFeasible() {
        int count = 0;
        int currentEndpoint = 2 * k;

        // This loop calculate the length of the cycle that the endpoint at removedEdges[2k] is connected
        // to by iterating the loop in reverse. We know that the successor of removedEdges[2k] is 0, which
        // give us our terminating condition.
        while (currentEndpoint != 0) {
            count++;
            currentEndpoint =
                    inverseRemovedEdgeIndexToTourOrder[addedEdgeToOtherEndpoint[removedEdgeIndexToTourOrder[currentEndpoint]]]
                            ^ 1;
        }
        return (count == k);
    }

    /**
     * Reverse an array between two indices and update its inverse array to point at the new locations.
     *
     * @param startInclusive Reverse the array starting at and including this index.
     * @param endExclusive Reverse the array ending at and excluding this index.
     */
    private void reversePermutationPart(int[] currentRemovedEdgeIndexToTourOrder,
            int[] currentInverseRemovedEdgeIndexToTourOrder,
            int startInclusive, int endExclusive) {
        for (; startInclusive < endExclusive; startInclusive++, endExclusive--) {
            int savedFirstElement = currentRemovedEdgeIndexToTourOrder[startInclusive];
            currentInverseRemovedEdgeIndexToTourOrder[currentRemovedEdgeIndexToTourOrder[startInclusive] =
                    currentRemovedEdgeIndexToTourOrder[endExclusive]] = startInclusive;
            currentInverseRemovedEdgeIndexToTourOrder[currentRemovedEdgeIndexToTourOrder[endExclusive] = savedFirstElement] =
                    endExclusive;
        }
    }

    /**
     * Calculate the "score" of performing a flip on a signed permutation p.<br>
     * <br>
     * Let p = (p_1 ..., p_n) be a signed permutation. An oriented pair (p_i, p_j) is a pair
     * of adjacent integers, that is |p_i| - |p_j| = ±1, with opposite signs. For example,
     * the signed permutation <br />
     * (+1 -2 -5 +4 +3) <br />
     * contains three oriented pairs: (+1, -2), (-2, +3), and (-5, +4).
     * Oriented pairs are useful as they indicate reversals that cause adjacent integers to be
     * consecutive in the resulting permutation. For example, the oriented pair (-2, +3) induces
     * the reversal <br>
     * (+1 -2 -5 +4 +3) -> (+1 -4 +5 +2 +3) <br>
     * creating a permutation where +3 is consecutive to +2. <br />
     * <br />
     * In general, the reversal induced by and oriented pair (p_i, p_j) is <br />
     * p(i, j-1), if p_i + p_j = +1, and <br />
     * p(i+1, j), if p_i + p_j = -1 <br />
     * Such a reversal is called an oriented reversal. <br />
     *
     * The score of an oriented reversal is defined as the number of oriented pairs
     * in the resulting permutation. <br />
     * <br />
     * This function perform the reversal indicated by the oriented pair, count the
     * number of oriented pairs in the new permutation, undo the reversal and return
     * the score.
     *
     * @param left The left endpoint of the flip
     * @param right the right endpoint of the flip
     * @return The score of the performing the signed reversal
     */
    private int countOrientedPairsForReversal(int[] currentRemovedEdgeIndexToTourOrder,
            int[] currentInverseRemovedEdgeIndexToTourOrder, int left, int right) {
        int count = 0, i, j;
        reversePermutationPart(
                currentRemovedEdgeIndexToTourOrder,
                currentInverseRemovedEdgeIndexToTourOrder,
                left, right);
        for (i = 1; i <= 2 * k - 2; i++) {
            j = currentInverseRemovedEdgeIndexToTourOrder[addedEdgeToOtherEndpoint[currentRemovedEdgeIndexToTourOrder[i]]];
            if (j >= i + 2 && (i & 1) == (j & 1)) {
                count++;
            }
        }
        reversePermutationPart(
                currentRemovedEdgeIndexToTourOrder,
                currentInverseRemovedEdgeIndexToTourOrder,
                left, right);
        return count;
    }

    /**
     * Get a {@link FlipSublistMove} that reverses the sublist that consists of the path
     * between the start and end of the given edges.
     *
     * @param listVariableDescriptor
     * @param indexVariableSupply
     * @param originalToCurrentIndexList
     * @param entity
     * @param firstEdgeStart
     * @param firstEdgeEnd
     * @param secondEdgeStart
     * @param secondEdgeEnd
     * @return
     */
    private FlipSublistMove<Solution_> getListReversalMoveForEdgePair(ListVariableDescriptor<Solution_> listVariableDescriptor,
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

    public String toString() {
        return k + "-opt(removed=[" + getRemovedEdges() + "]\n, added=[" + getAddedEdges() + "])";
    }
}
