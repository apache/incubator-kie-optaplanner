package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * Flips a sublist of a list variable, (the same thing as a {@link TwoOptListMove}, but no shift to restore the original
 * origin).
 * For instance, given [0, 1, 2, 3, 4], fromIndexInclusive = 1, toIndexExclusive = 3,
 * the list after the move would be [0, 3, 2, 1, 4].
 * If toIndexExclusive is before fromIndexInclusive,
 * the flip is performed on the combined sublists [fromIndexInclusive, size) and [0, toIndexExclusive).
 * For instance, given [0, 1, 2, 3, 4, 5, 6], fromIndexInclusive = 5, toIndexExclusive = 2,
 * the list after the move would be [6, 5, 2, 3, 4, 1, 0] (and not [0, 6, 5, 2, 3, 4, 1]).
 *
 * @param <Solution_>
 */
public class FlipSublistMove<Solution_> extends AbstractMove<Solution_> {
    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final Object entity;
    private final int fromIndexInclusive;
    private final int toIndexExclusive;

    public FlipSublistMove(ListVariableDescriptor<Solution_> variableDescriptor,
            Object entity,
            int fromIndexInclusive, int toIndexExclusive) {
        this.variableDescriptor = variableDescriptor;
        this.entity = entity;
        this.fromIndexInclusive = fromIndexInclusive;
        this.toIndexExclusive = toIndexExclusive;
    }

    @Override
    protected FlipSublistMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new FlipSublistMove<>(variableDescriptor, entity,
                fromIndexInclusive,
                toIndexExclusive);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = variableDescriptor.getListVariable(entity);

        if (fromIndexInclusive < toIndexExclusive) {
            // If the first endpoint is 0, we also need to rotate the entire list
            innerScoreDirector.beforeListVariableChanged(variableDescriptor, entity,
                    fromIndexInclusive,
                    toIndexExclusive);
        } else {
            innerScoreDirector.beforeListVariableChanged(variableDescriptor, entity,
                    fromIndexInclusive,
                    listVariable.size());
            innerScoreDirector.beforeListVariableChanged(variableDescriptor, entity,
                    0,
                    toIndexExclusive);
        }

        flipSublist(listVariable, fromIndexInclusive, toIndexExclusive);

        if (fromIndexInclusive < toIndexExclusive) {
            innerScoreDirector.afterListVariableChanged(variableDescriptor, entity,
                    fromIndexInclusive,
                    toIndexExclusive);
        } else {
            innerScoreDirector.afterListVariableChanged(variableDescriptor, entity,
                    fromIndexInclusive,
                    listVariable.size());
            innerScoreDirector.afterListVariableChanged(variableDescriptor, entity,
                    0,
                    toIndexExclusive);
        }
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return true;
    }

    @Override
    public FlipSublistMove<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        return new FlipSublistMove<>(variableDescriptor,
                destinationScoreDirector.lookUpWorkingObject(entity),
                fromIndexInclusive,
                toIndexExclusive);
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return "FlipSublist(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return List.of(entity);
    }

    @Override
    public Collection<?> getPlanningValues() {
        List<Object> listVariable = variableDescriptor.getListVariable(entity);

        if (fromIndexInclusive < toIndexExclusive) {
            return new ArrayList<>(listVariable.subList(fromIndexInclusive, toIndexExclusive));
        } else {
            List<Object> firstHalfReversedPath = listVariable.subList(fromIndexInclusive, listVariable.size());
            List<Object> secondHalfReversedPath = listVariable.subList(0, toIndexExclusive);
            return new ArrayList<>(firstHalfReversedPath.size() + secondHalfReversedPath.size());
        }
    }

    public Object getEntity() {
        return entity;
    }

    public static <T> void flipSublist(List<T> originalList, int fromIndexInclusive, int toIndexExclusive) {
        if (fromIndexInclusive < toIndexExclusive) {
            Collections.reverse(originalList.subList(fromIndexInclusive, toIndexExclusive));
        } else {
            List<T> firstHalfReversedPath = originalList.subList(fromIndexInclusive, originalList.size());
            List<T> secondHalfReversedPath = originalList.subList(0, toIndexExclusive);

            // Reverse the combined list firstHalfReversedPath + secondHalfReversedPath
            // For instance, (1, 2, 3)(4, 5, 6, 7, 8, 9) becomes
            // (9, 8, 7)(6, 5, 4, 3, 2, 1)
            int totalLength = firstHalfReversedPath.size() + secondHalfReversedPath.size();

            for (int i = 0; (i < totalLength >> 1); i++) {
                if (i < firstHalfReversedPath.size()) {
                    if (i < secondHalfReversedPath.size()) {
                        // firstHalfIndex = i
                        int secondHalfIndex = secondHalfReversedPath.size() - i - 1;
                        T savedFirstItem = firstHalfReversedPath.get(i);
                        firstHalfReversedPath.set(i, secondHalfReversedPath.get(secondHalfIndex));
                        secondHalfReversedPath.set(secondHalfIndex, savedFirstItem);
                    } else {
                        // firstIndex = i
                        int secondIndex = firstHalfReversedPath.size() - i + secondHalfReversedPath.size() - 1;
                        T savedFirstItem = firstHalfReversedPath.get(i);
                        firstHalfReversedPath.set(i, firstHalfReversedPath.get(secondIndex));
                        firstHalfReversedPath.set(secondIndex, savedFirstItem);
                    }
                } else {
                    int firstIndex = i - firstHalfReversedPath.size();
                    int secondIndex = secondHalfReversedPath.size() - i - 1;
                    T savedFirstItem = secondHalfReversedPath.get(firstIndex);
                    secondHalfReversedPath.set(firstIndex, secondHalfReversedPath.get(secondIndex));
                    secondHalfReversedPath.set(secondIndex, savedFirstItem);
                }
            }
        }
    }

    public static <T> void flipSubarray(int[] array, int fromIndexInclusive, int toIndexExclusive) {
        if (fromIndexInclusive < toIndexExclusive) {
            int length = (toIndexExclusive - fromIndexInclusive) >> 1;
            for (int i = 0; i < length; i++) {
                int index = fromIndexInclusive + i;
                int oppositeIndex = toIndexExclusive - i - 1;
                int saved = array[index];
                array[index] = array[oppositeIndex];
                array[oppositeIndex] = saved;
            }
        } else {
            int firstHalfSize = array.length - fromIndexInclusive;
            int secondHalfSize = toIndexExclusive;

            // Reverse the combined list firstHalfReversedPath + secondHalfReversedPath
            // For instance, (1, 2, 3)(4, 5, 6, 7, 8, 9) becomes
            // (9, 8, 7)(6, 5, 4, 3, 2, 1)
            int totalLength = firstHalfSize + secondHalfSize;

            // Used to rotate the list to put the first element back in its original position
            for (int i = 0; (i < totalLength >> 1); i++) {
                int firstHalfIndex;
                int secondHalfIndex;

                if (i < firstHalfSize) {
                    if (i < secondHalfSize) {
                        firstHalfIndex = fromIndexInclusive + i;
                        secondHalfIndex = secondHalfSize - i - 1;
                    } else {
                        firstHalfIndex = fromIndexInclusive + i;
                        secondHalfIndex = array.length - (i - secondHalfSize) - 1;
                    }
                } else {
                    firstHalfIndex = i - firstHalfSize;
                    secondHalfIndex = secondHalfSize - i - 1;
                }

                int saved = array[firstHalfIndex];
                array[firstHalfIndex] = array[secondHalfIndex];
                array[secondHalfIndex] = saved;
            }
        }
    }

    @Override
    public String toString() {
        return "FlipSublistMove(entity=" +
                entity +
                ", from=" + fromIndexInclusive + ", to=" + toIndexExclusive + ")";
    }
}
