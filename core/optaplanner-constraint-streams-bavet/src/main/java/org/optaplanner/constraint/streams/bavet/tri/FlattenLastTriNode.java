package org.optaplanner.constraint.streams.bavet.tri;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractFlattenLastNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class FlattenLastTriNode<A, B, C, NewC>
        extends AbstractFlattenLastNode<TriTupleImpl<A, B, C>, TriTupleImpl<A, B, NewC>, C, NewC> {

    private final int outputStoreSize;

    FlattenLastTriNode(int flattenLastStoreIndex, Function<C, Iterable<NewC>> mappingFunction,
            TupleLifecycle<TriTupleImpl<A, B, NewC>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(flattenLastStoreIndex, mappingFunction, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected TriTupleImpl<A, B, NewC> createTuple(TriTupleImpl<A, B, C> originalTuple, NewC newC) {
        return new TriTupleImpl<>(originalTuple.factA, originalTuple.factB, newC, outputStoreSize);
    }

    @Override
    protected C getEffectiveFactIn(TriTupleImpl<A, B, C> tuple) {
        return tuple.factC;
    }

    @Override
    protected NewC getEffectiveFactOut(TriTupleImpl<A, B, NewC> outTuple) {
        return outTuple.factC;
    }
}
