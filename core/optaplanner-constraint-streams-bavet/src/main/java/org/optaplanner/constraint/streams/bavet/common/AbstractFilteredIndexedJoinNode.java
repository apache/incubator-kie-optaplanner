package org.optaplanner.constraint.streams.bavet.common;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

/**
 * There is a strong likelihood that any change to this class, which is not related to indexing,
 * should also be made to {@link AbstractFilteredUnindexedJoinNode}.
 *
 * @param <LeftTuple_>
 * @param <Right_>
 */
public abstract class AbstractFilteredIndexedJoinNode<LeftTuple_ extends Tuple, Right_, OutTuple_ extends Tuple, MutableOutTuple_ extends OutTuple_>
        extends AbstractIndexedJoinNode<LeftTuple_, Right_, OutTuple_, MutableOutTuple_> {

    private final BiPredicate<LeftTuple_, UniTuple<Right_>> filtering = this::testFiltering; // Avoid creation in loops.

    protected AbstractFilteredIndexedJoinNode(Function<Right_, IndexProperties> mappingRight,
            int inputStoreIndexLeftProperties, int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightProperties, int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle, int outputStoreIndexLeftOutEntry,
            int outputStoreIndexRightOutEntry,
            Indexer<LeftTuple_> indexerLeft, Indexer<UniTuple<Right_>> indexerRight) {
        super(mappingRight, inputStoreIndexLeftProperties, inputStoreIndexLeftEntry, inputStoreIndexLeftOutTupleList,
                inputStoreIndexRightProperties, inputStoreIndexRightEntry, inputStoreIndexRightOutTupleList,
                nextNodesTupleLifecycle, outputStoreIndexLeftOutEntry, outputStoreIndexRightOutEntry, indexerLeft,
                indexerRight);
    }

    @Override
    protected void insertOutTupleMaybeFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple) {
        if (testFiltering(leftTuple, rightTuple)) {
            super.insertOutTupleMaybeFiltering(leftTuple, rightTuple);
        }
    }

    @Override
    protected void updateOutTupleLeftMaybeFiltering(TupleList<MutableOutTuple_> outTupleListLeft, LeftTuple_ leftTuple) {
        IndexProperties oldIndexProperties = leftTuple.getStore(inputStoreIndexLeftProperties);
        Map<TupleList<MutableOutTuple_>, MutableOutTuple_> rightToOutMap =
                buildOutMap(outTupleListLeft, outputStoreIndexRightOutEntry);
        indexerRight.forEach(oldIndexProperties,
                rightTuple -> doUpdateOutTupleLeft(rightToOutMap, leftTuple, rightTuple, filtering));
    }

    @Override
    protected void updateOutTupleRightMaybeFiltering(TupleList<MutableOutTuple_> outTupleListRight,
            UniTuple<Right_> rightTuple) {
        IndexProperties oldIndexProperties = rightTuple.getStore(inputStoreIndexRightProperties);
        Map<TupleList<MutableOutTuple_>, MutableOutTuple_> leftToOutMap =
                buildOutMap(outTupleListRight, outputStoreIndexLeftOutEntry);
        indexerLeft.forEach(oldIndexProperties,
                leftTuple -> doUpdateOutTupleRight(leftToOutMap, leftTuple, rightTuple, filtering));
    }

    protected abstract boolean testFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

}
