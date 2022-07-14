package org.optaplanner.constraint.streams.bavet.quad;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractFlattenLastNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class FlattenLastQuadNode<A, B, C, D, NewD>
        extends AbstractFlattenLastNode<QuadTupleImpl<A, B, C, D>, QuadTupleImpl<A, B, C, NewD>, D, NewD> {

    private final int outputStoreSize;

    FlattenLastQuadNode(int flattenLastStoreIndex, Function<D, Iterable<NewD>> mappingFunction,
            TupleLifecycle<QuadTupleImpl<A, B, C, NewD>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(flattenLastStoreIndex, mappingFunction, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected QuadTupleImpl<A, B, C, NewD> createTuple(QuadTupleImpl<A, B, C, D> originalTuple, NewD newD) {
        return new QuadTupleImpl<>(originalTuple.factA, originalTuple.factB, originalTuple.factC, newD, outputStoreSize);
    }

    @Override
    protected D getEffectiveFactIn(QuadTupleImpl<A, B, C, D> tuple) {
        return tuple.factD;
    }

    @Override
    protected NewD getEffectiveFactOut(QuadTupleImpl<A, B, C, NewD> outTuple) {
        return outTuple.factD;
    }
}
