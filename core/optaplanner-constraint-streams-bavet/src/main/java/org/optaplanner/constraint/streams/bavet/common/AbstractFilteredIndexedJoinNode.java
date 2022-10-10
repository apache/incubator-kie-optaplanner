package org.optaplanner.constraint.streams.bavet.common;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.collection.TupleList;
import org.optaplanner.constraint.streams.bavet.common.collection.TupleListEntry;
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
        // Hack: the outTuple has no left/right input tuple reference, use the left/right outList reference instead
        Map<TupleList<MutableOutTuple_>, MutableOutTuple_> rightToOutMap =
                new IdentityHashMap<>(outTupleListLeft.size());
        outTupleListLeft.forEach(outTuple -> {
            TupleListEntry<MutableOutTuple_> rightOutEntry = outTuple.getStore(outputStoreIndexRightOutEntry);
            rightToOutMap.put(rightOutEntry.getList(), outTuple);
        });
        indexerRight.forEach(oldIndexProperties, (rightTuple) -> {
            TupleList<MutableOutTuple_> rightOutList = rightTuple.getStore(inputStoreIndexRightOutTupleList);
            MutableOutTuple_ outTuple = rightToOutMap.get(rightOutList);
            if (testFiltering(leftTuple, rightTuple)) {
                if (outTuple == null) {
                    insertOutTuple(leftTuple, rightTuple);
                } else {
                    updateOutTupleLeft(outTuple, leftTuple);
                }
            } else {
                if (outTuple != null) {
                    retractOutTuple(outTuple);
                }
            }
        });
    }

    @Override
    protected void updateOutTupleRightMaybeFiltering(TupleList<MutableOutTuple_> outTupleListRight,
            UniTuple<Right_> rightTuple) {
        IndexProperties oldIndexProperties = rightTuple.getStore(inputStoreIndexRightProperties);
        // Hack: the outTuple has no left/right input tuple reference, use the left/right outList reference instead
        Map<TupleList<MutableOutTuple_>, MutableOutTuple_> leftToOutMap =
                new IdentityHashMap<>(outTupleListRight.size());
        outTupleListRight.forEach(outTuple -> {
            TupleListEntry<MutableOutTuple_> leftOutEntry = outTuple.getStore(outputStoreIndexLeftOutEntry);
            leftToOutMap.put(leftOutEntry.getList(), outTuple);
        });
        indexerLeft.forEach(oldIndexProperties, (leftTuple) -> {
            TupleList<MutableOutTuple_> leftOutList = leftTuple.getStore(inputStoreIndexLeftOutTupleList);
            MutableOutTuple_ outTuple = leftToOutMap.get(leftOutList);
            if (testFiltering(leftTuple, rightTuple)) {
                if (outTuple == null) {
                    insertOutTuple(leftTuple, rightTuple);
                } else {
                    updateOutTupleRight(outTuple, rightTuple);
                }
            } else {
                if (outTuple != null) {
                    retractOutTuple(outTuple);
                }
            }
        });
    }

    protected abstract boolean testFiltering(LeftTuple_ leftTuple, UniTuple<Right_> rightTuple);

}
