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
 * @param <Node_> The value type
 */
public class KOptListMove<Solution_, Node_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final Object entity;
    private final KOptDescriptor<Solution_, Node_> descriptor;
    private final List<FlipSublistMove<Solution_>> equivalent2Opts;
    private final int postShiftAmount;

    public KOptListMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
            Object entity,
            KOptDescriptor<Solution_, Node_> descriptor,
            List<FlipSublistMove<Solution_>> equivalent2Opts,
            int postShiftAmount) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.entity = entity;
        this.descriptor = descriptor;
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
            return new UndoKOptListMove<>(listVariableDescriptor, entity, descriptor, inverse2Opts, -postShiftAmount);
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
                descriptor, rebasedEquivalent2Opts, postShiftAmount);
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return descriptor.getK() + "-opt(" + listVariableDescriptor.getSimpleEntityAndVariableName() + ")";
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

    @SuppressWarnings("unchecked")
    public static <Solution_, Node_> Function<Node_, Node_> getSuccessorFunction(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply) {
        return (node) -> {
            List<Node_> valueList =
                    (List<Node_>) listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
            int index = indexVariableSupply.getIndex(node);
            if (index == valueList.size() - 1) {
                return valueList.get(0);
            } else {
                return valueList.get(index + 1);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <Solution_, Node_> Function<Node_, Node_> getPredecessorFunction(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply) {
        return (node) -> {
            List<Node_> valueList =
                    (List<Node_>) listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
            int index = indexVariableSupply.getIndex(node);
            if (index == 0) {
                return valueList.get(valueList.size() - 1);
            } else {
                return valueList.get(index - 1);
            }
        };
    }

    public static <Node_> TriPredicate<Node_, Node_, Node_> getBetweenPredicate(IndexVariableSupply indexVariableSupply) {
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
        return descriptor.toString();
    }

    /**
     * A K-Opt move that does the list rotation before performing the flips instead of after, allowing
     * it to act as the undo move of a K-Opt move that does the rotation after the flips.
     *
     * @param <Solution_>
     */
    private static final class UndoKOptListMove<Solution_, Node_> extends AbstractMove<Solution_> {
        private final ListVariableDescriptor<Solution_> listVariableDescriptor;
        private final Object entity;
        private final KOptDescriptor<Solution_, Node_> descriptor;
        private final List<FlipSublistMove<Solution_>> equivalent2Opts;
        private final int preShiftAmount;

        public UndoKOptListMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
                Object entity,
                KOptDescriptor<Solution_, Node_> descriptor,
                List<FlipSublistMove<Solution_>> equivalent2Opts,
                int preShiftAmount) {
            this.listVariableDescriptor = listVariableDescriptor;
            this.entity = entity;
            this.descriptor = descriptor;
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

        public String toString() {
            return "Undo" + descriptor.toString();
        }
    }

}
