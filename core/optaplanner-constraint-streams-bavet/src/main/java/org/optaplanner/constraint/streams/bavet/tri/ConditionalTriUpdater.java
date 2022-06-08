package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.AbstractUpdater;
import org.optaplanner.core.api.function.TriPredicate;

import java.util.function.Consumer;

final class ConditionalTriUpdater<A, B, C> extends AbstractUpdater<TriTuple<A, B, C>> {
    private final TriPredicate<A, B, C> predicate;

    public ConditionalTriUpdater(TriPredicate<A, B, C> predicate, Consumer<TriTuple<A, B, C>> update,
                                 Consumer<TriTuple<A, B, C>> retract) {
        super(update, retract);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(TriTuple<A, B, C> tuple) {
        return predicate.test(tuple.factA, tuple.factB, tuple.factC);
    }

}
