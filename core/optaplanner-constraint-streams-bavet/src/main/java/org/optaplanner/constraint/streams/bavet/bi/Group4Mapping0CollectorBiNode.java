package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.quad.QuadTuple;
import org.optaplanner.constraint.streams.bavet.quad.QuadTupleImpl;
import org.optaplanner.core.impl.util.Quadruple;

final class Group4Mapping0CollectorBiNode<OldA, OldB, A, B, C, D>
        extends
        AbstractGroupBiNode<OldA, OldB, QuadTuple<A, B, C, D>, QuadTupleImpl<A, B, C, D>, Quadruple<A, B, C, D>, Void, Void> {

    private final int outputStoreSize;

    public Group4Mapping0CollectorBiNode(BiFunction<OldA, OldB, A> groupKeyMappingA, BiFunction<OldA, OldB, B> groupKeyMappingB,
            BiFunction<OldA, OldB, C> groupKeyMappingC, BiFunction<OldA, OldB, D> groupKeyMappingD, int groupStoreIndex,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(groupStoreIndex,
                tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, groupKeyMappingC, groupKeyMappingD, tuple),
                null, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    private static <A, B, C, D, OldA, OldB> Quadruple<A, B, C, D> createGroupKey(
            BiFunction<OldA, OldB, A> groupKeyMappingA, BiFunction<OldA, OldB, B> groupKeyMappingB,
            BiFunction<OldA, OldB, C> groupKeyMappingC, BiFunction<OldA, OldB, D> groupKeyMappingD,
            BiTuple<OldA, OldB> tuple) {
        OldA oldA = tuple.getFactA();
        OldB oldB = tuple.getFactB();
        A a = groupKeyMappingA.apply(oldA, oldB);
        B b = groupKeyMappingB.apply(oldA, oldB);
        C c = groupKeyMappingC.apply(oldA, oldB);
        D d = groupKeyMappingD.apply(oldA, oldB);
        return Quadruple.of(a, b, c, d);
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(Quadruple<A, B, C, D> groupKey) {
        return new QuadTupleImpl<>(groupKey.getA(), groupKey.getB(), groupKey.getC(), groupKey.getD(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(QuadTupleImpl<A, B, C, D> outTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
