package org.optaplanner.constraint.streams.bavet.uni;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Pair;

final class Group0Mapping2CollectorUniNode<OldA, A, B, ResultContainerA_, ResultContainerB_>
        extends AbstractGroupUniNode<OldA, BiTuple<A, B>, Void, Object, Pair<A, B>> {

    private final int outputStoreSize;

    public Group0Mapping2CollectorUniNode(int groupStoreIndex, int undoStoreIndex,
            UniConstraintCollector<OldA, ResultContainerA_, A> collectorA,
            UniConstraintCollector<OldA, ResultContainerB_, B> collectorB,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, null, mergeCollectors(collectorA, collectorB), nextNodesTupleLifecycle,
                environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <OldA, A, B, ResultContainerA_, ResultContainerB_>
            UniConstraintCollector<OldA, Object, Pair<A, B>> mergeCollectors(
                    UniConstraintCollector<OldA, ResultContainerA_, A> collectorA,
                    UniConstraintCollector<OldA, ResultContainerB_, B> collectorB) {
        return (UniConstraintCollector<OldA, Object, Pair<A, B>>) ConstraintCollectors.compose(collectorA, collectorB,
                Pair::of);
    }

    @Override
    protected BiTuple<A, B> createOutTuple(Void groupKey) {
        return BiTuple.of(null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTuple<A, B> outTuple, Pair<A, B> result) {
        outTuple.fillFrom(result);
    }

}
