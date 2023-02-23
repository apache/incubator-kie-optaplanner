package org.optaplanner.constraint.streams.bavet.uni;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.quad.QuadTuple;
import org.optaplanner.constraint.streams.bavet.quad.QuadTupleImpl;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.util.Quadruple;

final class Group0Mapping4CollectorUniNode<OldA, A, B, C, D, ResultContainerA_, ResultContainerB_, ResultContainerC_, ResultContainerD_>
        extends
        AbstractGroupUniNode<OldA, QuadTuple<A, B, C, D>, QuadTupleImpl<A, B, C, D>, Void, Object, Quadruple<A, B, C, D>> {

    private final int outputStoreSize;

    public Group0Mapping4CollectorUniNode(int groupStoreIndex, int undoStoreIndex,
            UniConstraintCollector<OldA, ResultContainerA_, A> collectorA,
            UniConstraintCollector<OldA, ResultContainerB_, B> collectorB,
            UniConstraintCollector<OldA, ResultContainerC_, C> collectorC,
            UniConstraintCollector<OldA, ResultContainerD_, D> collectorD,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(groupStoreIndex, undoStoreIndex, null, mergeCollectors(collectorA, collectorB, collectorC, collectorD),
                nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    private static <OldA, A, B, C, D, ResultContainerA_, ResultContainerB_, ResultContainerC_, ResultContainerD_>
            UniConstraintCollector<OldA, Object, Quadruple<A, B, C, D>> mergeCollectors(
                    UniConstraintCollector<OldA, ResultContainerA_, A> collectorA,
                    UniConstraintCollector<OldA, ResultContainerB_, B> collectorB,
                    UniConstraintCollector<OldA, ResultContainerC_, C> collectorC,
                    UniConstraintCollector<OldA, ResultContainerD_, D> collectorD) {
        return (UniConstraintCollector<OldA, Object, Quadruple<A, B, C, D>>) ConstraintCollectors.compose(collectorA,
                collectorB, collectorC, collectorD, Quadruple::of);
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(Void groupKey) {
        return new QuadTupleImpl<>(null, null, null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(QuadTupleImpl<A, B, C, D> outTuple, Quadruple<A, B, C, D> result) {
        outTuple.factA = result.getA();
        outTuple.factB = result.getB();
        outTuple.factC = result.getC();
        outTuple.factD = result.getD();
    }

}
