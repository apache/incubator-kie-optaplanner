package org.optaplanner.constraint.streams.bavet.bi;

import static org.optaplanner.constraint.streams.bavet.bi.Group0Mapping3CollectorBiNode.mergeCollectors;
import static org.optaplanner.constraint.streams.bavet.bi.Group1Mapping0CollectorBiNode.createGroupKey;

import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.QuadTuple;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Triple;

final class Group1Mapping3CollectorBiNode<OldA, OldB, A, B, C, D, ResultContainerB_, ResultContainerC_, ResultContainerD_>
        extends AbstractGroupBiNode<OldA, OldB, QuadTuple<A, B, C, D>, A, Object, Triple<B, C, D>> {

    private final int outputStoreSize;

    public Group1Mapping3CollectorBiNode(BiFunction<OldA, OldB, A> groupKeyMapping, int groupStoreIndex, int undoStoreIndex,
            BiConstraintCollector<OldA, OldB, ResultContainerB_, B> collectorB,
            BiConstraintCollector<OldA, OldB, ResultContainerC_, C> collectorC,
            BiConstraintCollector<OldA, OldB, ResultContainerD_, D> collectorD,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle, int outputStoreSize,
            EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple),
                mergeCollectors(collectorB, collectorC, collectorD), nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected QuadTuple<A, B, C, D> createOutTuple(A a) {
        return QuadTuple.of(a, null, null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(QuadTuple<A, B, C, D> outTuple, Triple<B, C, D> result) {
        outTuple.fillTailFrom(result);
    }

}
