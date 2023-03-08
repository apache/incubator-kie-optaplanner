package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.tri.TriTuple;
import org.optaplanner.constraint.streams.bavet.tri.TriTupleImpl;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Triple;

final class Group0Mapping3CollectorQuadNode<OldA, OldB, OldC, OldD, A, B, C, ResultContainerA_, ResultContainerB_, ResultContainerC_>
        extends
        AbstractGroupQuadNode<OldA, OldB, OldC, OldD, TriTuple<A, B, C>, TriTupleImpl<A, B, C>, Void, Object, Triple<A, B, C>> {

    private final int outputStoreSize;

    public Group0Mapping3CollectorQuadNode(int groupStoreIndex, int undoStoreIndex,
            QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerA_, A> collectorA,
            QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerB_, B> collectorB,
            QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerC_, C> collectorC,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, null, mergeCollectors(collectorA, collectorB, collectorC),
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <OldA, OldB, OldC, OldD, A, B, C, ResultContainerA_, ResultContainerB_, ResultContainerC_>
            QuadConstraintCollector<OldA, OldB, OldC, OldD, Object, Triple<A, B, C>> mergeCollectors(
                    QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerA_, A> collectorA,
                    QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerB_, B> collectorB,
                    QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerC_, C> collectorC) {
        return (QuadConstraintCollector<OldA, OldB, OldC, OldD, Object, Triple<A, B, C>>) ConstraintCollectors.compose(
                collectorA,
                collectorB, collectorC, Triple::of);
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(Void groupKey) {
        return new TriTupleImpl<>(null, null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(TriTupleImpl<A, B, C> outTuple, Triple<A, B, C> result) {
        outTuple.factA = result.getA();
        outTuple.factB = result.getB();
        outTuple.factC = result.getC();
    }

}
