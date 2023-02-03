package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class KOptListMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final Object entity;
    private final int k;
    private final List<FlipSublistMove<Solution_>> equivalent2Opts;
    private final int postShiftAmount;

    public KOptListMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
            Object entity,
            int k, List<FlipSublistMove<Solution_>> equivalent2Opts, int postShiftAmount) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.entity = entity;
        this.k = k;
        this.equivalent2Opts = equivalent2Opts;
        this.postShiftAmount = postShiftAmount;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        if (equivalent2Opts.isEmpty()) {
            return this;
        } else {
            List<FlipSublistMove<Solution_>> inverse2Opts = new ArrayList<>(equivalent2Opts.size());
            for (int i = equivalent2Opts.size() - 1; i >= 0; i--) {
                inverse2Opts.add(equivalent2Opts.get(i).createUndoMove(scoreDirector));
            }
            return new UndoKOptListMove<>(listVariableDescriptor, entity, k, inverse2Opts, -postShiftAmount);
        }
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        equivalent2Opts.forEach(move -> move.doMoveOnGenuineVariables(scoreDirector));
        rotateToOriginalPositions(scoreDirector, listVariableDescriptor, entity, postShiftAmount);
    }

    private static <Solution_> void rotateToOriginalPositions(ScoreDirector<Solution_> scoreDirector,
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            Object entity, int shiftAmount) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = listVariableDescriptor.getListVariable(entity);

        innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity, 0, listVariable.size());

        Collections.rotate(listVariable, shiftAmount);

        innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity, 0, listVariable.size());
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return !equivalent2Opts.isEmpty();
    }

    @Override
    public Move<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        List<FlipSublistMove<Solution_>> rebasedEquivalent2Opts = new ArrayList<>(equivalent2Opts.size());
        for (FlipSublistMove<Solution_> twoOpt : equivalent2Opts) {
            rebasedEquivalent2Opts.add(twoOpt.rebase(destinationScoreDirector));
        }
        return new KOptListMove<>(listVariableDescriptor, destinationScoreDirector.lookUpWorkingObject(entity),
                k, rebasedEquivalent2Opts, postShiftAmount);
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return k + "-opt(" + listVariableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return List.of(equivalent2Opts.get(0).getEntity());
    }

    @Override
    public Collection<?> getPlanningValues() {
        return equivalent2Opts.stream().flatMap(twoOpt -> twoOpt.getPlanningValues().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Create a sequential or non-sequential k-opt from the supplied pairs of undirected removed and added edges.
     *
     * @param listVariableDescriptor
     * @param indexVariableSupply
     * @param entity The entity
     * @param removedEdgeList The edges to remove. For each pair {@code (edgePairs[2*i], edgePairs[2*i+1])},
     *        it must be the case {@code edgePairs[2*i+1]} is either the successor or predecessor of
     *        {@code edgePairs[2*i]}. Additionally, each edge must belong to the given entity's
     *        list variable.
     * @param addedEdgeList The edges to add. Must contain only endpoints specified in the removedEdgeList.
     * @return A new sequential or non-sequential k-opt move with the specified undirected edges removed and added.
     * @param <Solution_>
     */
    public static <Solution_> KOptListMove<Solution_> fromRemovedAndAddedEdges(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            IndexVariableSupply indexVariableSupply,
            Object entity,
            List<Object> removedEdgeList,
            List<Object> addedEdgeList) {

        if (addedEdgeList.size() != removedEdgeList.size()) {
            throw new IllegalArgumentException(
                    "addedEdgeList (" + addedEdgeList + ") and removedEdgeList (" + removedEdgeList + ") have the same size");
        }

        if ((addedEdgeList.size() % 2) != 0) {
            throw new IllegalArgumentException(
                    "addedEdgeList and removedEdgeList are invalid: there is an odd number of endpoints.");
        }

        if (!addedEdgeList.containsAll(removedEdgeList)) {
            throw new IllegalArgumentException("addedEdgeList (" + addedEdgeList + ") is invalid; it contains endpoints "
                    + "that are not included in the removedEdgeList (" + removedEdgeList + ").");
        }

        Function<Object, Object> successorFunction =
                getSuccessorFunction(listVariableDescriptor, ignored -> entity, indexVariableSupply);

        for (int i = 0; i < removedEdgeList.size(); i += 2) {
            if (successorFunction.apply(removedEdgeList.get(i)) != removedEdgeList.get(i + 1)
                    && successorFunction.apply(removedEdgeList.get(i + 1)) != removedEdgeList.get(i)) {
                throw new IllegalArgumentException("removedEdgeList (" + removedEdgeList + ") contains an invalid edge ((" +
                        removedEdgeList.get(i) + ", " + removedEdgeList.get(i + 1) + ")).");
            }
        }

        Object[] tourArray = new Object[removedEdgeList.size() + 1];
        Integer[] incl = new Integer[removedEdgeList.size() + 1];
        for (int i = 0; i < removedEdgeList.size(); i += 2) {
            tourArray[i + 1] = removedEdgeList.get(i);
            tourArray[i + 2] = removedEdgeList.get(i + 1);
            int addedEdgeIndex = identityIndexOf(addedEdgeList, removedEdgeList.get(i));

            if (addedEdgeIndex % 2 == 0) {
                incl[i + 1] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex + 1)) + 1;
            } else {
                incl[i + 1] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex - 1)) + 1;
            }

            addedEdgeIndex = identityIndexOf(addedEdgeList, removedEdgeList.get(i + 1));
            if (addedEdgeIndex % 2 == 0) {
                incl[i + 2] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex + 1)) + 1;
            } else {
                incl[i + 2] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex - 1)) + 1;
            }
        }

        KOptDescriptor<Solution_> descriptor = new KOptDescriptor<>(tourArray,
                incl,
                getSuccessorFunction(listVariableDescriptor,
                        ignored -> entity,
                        indexVariableSupply),
                getBetweenPredicate(indexVariableSupply));
        return descriptor.getKOptListMove(listVariableDescriptor, indexVariableSupply, entity);
    }

    private static int identityIndexOf(List<Object> sourceList, Object query) {
        for (int i = 0; i < sourceList.size(); i++) {
            if (sourceList.get(i) == query) {
                return i;
            }
        }
        return -1;
    }

    public static <Solution_> Function<Object, Object> getSuccessorFunction(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply) {
        return (node) -> {
            List<Object> valueList = listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
            int index = indexVariableSupply.getIndex(node);
            if (index == valueList.size() - 1) {
                return valueList.get(0);
            } else {
                return valueList.get(index + 1);
            }
        };
    }

    public static <Solution_> Function<Object, Object> getPredecessorFunction(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply) {
        return (node) -> {
            List<Object> valueList = listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
            int index = indexVariableSupply.getIndex(node);
            if (index == 0) {
                return valueList.get(valueList.size() - 1);
            } else {
                return valueList.get(index - 1);
            }
        };
    }

    public static TriPredicate<Object, Object, Object> getBetweenPredicate(IndexVariableSupply indexVariableSupply) {
        return (start, middle, end) -> {
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
        };
    }

    public String toString() {
        return k + "-opt(equivalent2Opt="
                + equivalent2Opts.stream().map(FlipSublistMove::toString).collect(Collectors.joining(", ", "[", "]"))
                + ")";
    }

    /**
     * A K-Opt move that does the list rotation before performing the flips instead of after, allowing
     * it to act as the undo move of a K-Opt move that does the rotation after the flips.
     *
     * @param <Solution_>
     */
    private static final class UndoKOptListMove<Solution_> extends AbstractMove<Solution_> {
        private final ListVariableDescriptor<Solution_> listVariableDescriptor;
        private final Object entity;
        private final int k;
        private final List<FlipSublistMove<Solution_>> equivalent2Opts;
        private final int preShiftAmount;

        public UndoKOptListMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
                Object entity,
                int k, List<FlipSublistMove<Solution_>> equivalent2Opts, int preShiftAmount) {
            this.listVariableDescriptor = listVariableDescriptor;
            this.entity = entity;
            this.k = k;
            this.equivalent2Opts = equivalent2Opts;
            this.preShiftAmount = preShiftAmount;
        }

        @Override
        public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
            return true;
        }

        @Override
        protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
            rotateToOriginalPositions(scoreDirector, listVariableDescriptor, entity, preShiftAmount);
            equivalent2Opts.forEach(twoOpt -> twoOpt.doMoveOnGenuineVariables(scoreDirector));
        }
    }

}
