package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.BiPredicate;

import org.optaplanner.constraint.streams.bavet.common.AbstractConditionalTupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;

final class ConditionalBiTupleLifecycle<A, B> extends AbstractConditionalTupleLifecycle<BiTupleImpl<A, B>> {
    private final BiPredicate<A, B> predicate;

    public ConditionalBiTupleLifecycle(BiPredicate<A, B> predicate, TupleLifecycle<BiTupleImpl<A, B>> tupleLifecycle) {
        super(tupleLifecycle);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(BiTupleImpl<A, B> tuple) {
        return predicate.test(tuple.factA, tuple.factB);
    }

}
