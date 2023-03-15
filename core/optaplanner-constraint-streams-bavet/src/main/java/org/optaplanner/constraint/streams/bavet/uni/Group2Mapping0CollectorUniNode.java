package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Pair;

final class Group2Mapping0CollectorUniNode<OldA, A, B>
        extends AbstractGroupUniNode<OldA, BiTuple<A, B>, Pair<A, B>, Void, Void> {

    private final int outputStoreSize;

    public Group2Mapping0CollectorUniNode(Function<OldA, A> groupKeyMappingA, Function<OldA, B> groupKeyMappingB,
            int groupStoreIndex, TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize,
            EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, tuple), nextNodesTupleLifecycle,
                environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, B, OldA> Pair<A, B> createGroupKey(Function<OldA, A> groupKeyMappingA, Function<OldA, B> groupKeyMappingB,
            UniTuple<OldA> tuple) {
        OldA oldA = tuple.getA();
        A a = groupKeyMappingA.apply(oldA);
        B b = groupKeyMappingB.apply(oldA);
        return Pair.of(a, b);
    }

    @Override
    protected BiTuple<A, B> createOutTuple(Pair<A, B> groupKey) {
        return BiTuple.of(groupKey.getKey(), groupKey.getValue(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTuple<A, B> outTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
