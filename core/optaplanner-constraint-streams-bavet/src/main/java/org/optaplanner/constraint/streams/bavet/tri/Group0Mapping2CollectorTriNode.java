package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.bi.BiTuple;
import org.optaplanner.constraint.streams.bavet.bi.BiTupleImpl;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.util.Pair;

final class Group0Mapping2CollectorTriNode<OldA, OldB, OldC, A, B, ResultContainerA_, ResultContainerB_>
        extends AbstractGroupTriNode<OldA, OldB, OldC, BiTuple<A, B>, BiTupleImpl<A, B>, Void, Object, Pair<A, B>> {

    private final int outputStoreSize;

    public Group0Mapping2CollectorTriNode(int groupStoreIndex, int undoStoreIndex,
            TriConstraintCollector<OldA, OldB, OldC, ResultContainerA_, A> collectorA,
            TriConstraintCollector<OldA, OldB, OldC, ResultContainerB_, B> collectorB,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(groupStoreIndex, undoStoreIndex, null, mergeCollectors(collectorA, collectorB), nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    static <OldA, OldB, OldC, A, B, ResultContainerA_, ResultContainerB_>
            TriConstraintCollector<OldA, OldB, OldC, Object, Pair<A, B>> mergeCollectors(
                    TriConstraintCollector<OldA, OldB, OldC, ResultContainerA_, A> collectorA,
                    TriConstraintCollector<OldA, OldB, OldC, ResultContainerB_, B> collectorB) {
        return (TriConstraintCollector<OldA, OldB, OldC, Object, Pair<A, B>>) ConstraintCollectors.compose(collectorA,
                collectorB,
                Pair::of);
    }

    @Override
    protected BiTupleImpl<A, B> createOutTuple(Void groupKey) {
        return new BiTupleImpl<>(null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTupleImpl<A, B> outTuple, Pair<A, B> result) {
        outTuple.factA = result.getKey();
        outTuple.factB = result.getValue();
    }

}
