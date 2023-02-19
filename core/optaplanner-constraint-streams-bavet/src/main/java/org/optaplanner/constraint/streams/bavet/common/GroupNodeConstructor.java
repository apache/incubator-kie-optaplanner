package org.optaplanner.constraint.streams.bavet.common;

import java.util.List;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintStream;

@FunctionalInterface
public interface GroupNodeConstructor<Tuple_ extends Tuple> {

    AbstractNode apply(int groupStoreIndex, int undoStoreIndex, TupleLifecycle<Tuple_> nextNodesTupleLifecycle,
            int outputStoreSize);

    static <Score_ extends Score<Score_>, Tuple_ extends Tuple> void build(GroupNodeConstructor<Tuple_> nodeConstructor,
            NodeBuildHelper<Score_> buildHelper, ConstraintStream parentTupleSource, ConstraintStream groupStream,
            List<? extends ConstraintStream> groupStreamChildList, ConstraintStream thisStream,
            List<? extends ConstraintStream> thisStreamChildList) {
        if (!thisStreamChildList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + thisStream
                    + ") has an non-empty childStreamList (" + thisStreamChildList + ") but it's a groupBy bridge.");
        }
        int groupStoreIndex = buildHelper.reserveTupleStoreIndex(parentTupleSource);
        int undoStoreIndex = buildHelper.reserveTupleStoreIndex(parentTupleSource);
        TupleLifecycle<Tuple_> tupleLifecycle = buildHelper.getAggregatedTupleLifecycle(groupStreamChildList);
        int outputStoreSize = buildHelper.extractTupleStoreSize(groupStream);
        var node = nodeConstructor.apply(groupStoreIndex, undoStoreIndex, tupleLifecycle, outputStoreSize);
        buildHelper.addNode(node, thisStream);
    }

}
