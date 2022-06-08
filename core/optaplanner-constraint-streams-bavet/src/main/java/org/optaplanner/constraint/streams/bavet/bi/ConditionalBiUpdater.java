package org.optaplanner.constraint.streams.bavet.bi;

import org.optaplanner.constraint.streams.bavet.common.AbstractUpdater;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

final class ConditionalBiUpdater<A, B> extends AbstractUpdater<BiTuple<A, B>> {
    private final BiPredicate<A, B> predicate;

    public ConditionalBiUpdater(BiPredicate<A, B> predicate, Consumer<BiTuple<A, B>> update,
                                Consumer<BiTuple<A, B>> retract) {
        super(update, retract);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(BiTuple<A, B> tuple) {
        return predicate.test(tuple.factA, tuple.factB);
    }

}
