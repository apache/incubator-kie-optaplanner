package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.AbstractConditionalTupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.core.api.function.QuadPredicate;

final class ConditionalQuadTupleLifecycle<A, B, C, D> extends AbstractConditionalTupleLifecycle<QuadTupleImpl<A, B, C, D>> {
    private final QuadPredicate<A, B, C, D> predicate;

    public ConditionalQuadTupleLifecycle(QuadPredicate<A, B, C, D> predicate,
            TupleLifecycle<QuadTupleImpl<A, B, C, D>> tupleLifecycle) {
        super(tupleLifecycle);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(QuadTupleImpl<A, B, C, D> tuple) {
        return predicate.test(tuple.factA, tuple.factB, tuple.factC, tuple.factD);
    }
}
