package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractFlattenLastNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class FlattenLastBiNode<A, B, NewB> extends AbstractFlattenLastNode<BiTupleImpl<A, B>, BiTupleImpl<A, NewB>, B, NewB> {

    private final int outputStoreSize;

    FlattenLastBiNode(int flattenLastStoreIndex, Function<B, Iterable<NewB>> mappingFunction,
            TupleLifecycle<BiTupleImpl<A, NewB>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(flattenLastStoreIndex, mappingFunction, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected BiTupleImpl<A, NewB> createTuple(BiTupleImpl<A, B> originalTuple, NewB newB) {
        return new BiTupleImpl<>(originalTuple.factA, newB, outputStoreSize);
    }

    @Override
    protected B getEffectiveFactIn(BiTupleImpl<A, B> tuple) {
        return tuple.factB;
    }

    @Override
    protected NewB getEffectiveFactOut(BiTupleImpl<A, NewB> outTuple) {
        return outTuple.factB;
    }
}
