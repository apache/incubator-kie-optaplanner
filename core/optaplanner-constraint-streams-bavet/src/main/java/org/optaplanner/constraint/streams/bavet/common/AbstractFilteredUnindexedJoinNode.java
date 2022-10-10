package org.optaplanner.constraint.streams.bavet.common;

import java.util.Map;
import java.util.function.BiPredicate;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change made to this class
 * should also be made to {@link AbstractFilteredIndexedJoinNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractFilteredUnindexedJoinNode<LeftTuple_ extends Tuple, Right_, OutTuple_ extends Tuple, MutableOutTuple_ extends OutTuple_>
        extends AbstractUnindexedJoinNode<LeftTuple_, Right_, OutTuple_, MutableOutTuple_> {

    private final BiPredicate<LeftTuple_, UniTuple<Right_>> filtering = this::testFiltering; // Avoid creation in loops.

    protected AbstractFilteredUnindexedJoinNode(int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle, int outputStoreIndexLeftOutEntry,
            int outputStoreIndexRightOutEntry) {
        super(inputStoreIndexLeftEntry, inputStoreIndexLeftOutTupleList, inputStoreIndexRightEntry,
                inputStoreIndexRightOutTupleList, nextNodesTupleLifecycle, outputStoreIndexLeftOutEntry,
                outputStoreIndexRightOutEntry);
    }

    @Override
    protected void insertOutTupleMaybeFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple) {
        if (testFiltering(leftTuple, rightTuple)) {
            super.insertOutTupleMaybeFiltering(leftTuple, rightTuple);
        }
    }

    @Override
    protected void updateOutTupleLeftMaybeFiltering(TupleList<MutableOutTuple_> outTupleListLeft, LeftTuple_ leftTuple) {
        Map<TupleList<MutableOutTuple_>, MutableOutTuple_> rightToOutMap =
                buildOutMap(outTupleListLeft, outputStoreIndexRightOutEntry);
        for (UniTuple<Right_> rightTuple : rightTupleList) {
            doUpdateOutTupleLeft(rightToOutMap, leftTuple, rightTuple, filtering);
        }
    }

    @Override
    protected void updateOutTupleRightMaybeFiltering(TupleList<MutableOutTuple_> outTupleListRight,
            UniTuple<Right_> rightTuple) {
        Map<TupleList<MutableOutTuple_>, MutableOutTuple_> leftToOutMap =
                buildOutMap(outTupleListRight, outputStoreIndexLeftOutEntry);
        for (LeftTuple_ leftTuple : leftTupleList) {
            doUpdateOutTupleRight(leftToOutMap, leftTuple, rightTuple, filtering);
        }
    }

    protected abstract boolean testFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

}
