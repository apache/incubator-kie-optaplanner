package org.optaplanner.constraint.streams.bavet.uni;

import org.optaplanner.constraint.streams.bavet.common.AbstractUpdater;

import java.util.function.Consumer;
import java.util.function.Predicate;

final class ConditionalUniUpdater<A> extends AbstractUpdater<UniTuple<A>> {
    private final Predicate<A> predicate;

    public ConditionalUniUpdater(Predicate<A> predicate, Consumer<UniTuple<A>> update,
                                 Consumer<UniTuple<A>> retract) {
        super(update, retract);
        this.predicate = predicate;
    }

    @Override
    protected boolean test(UniTuple<A> tuple) {
        return predicate.test(tuple.factA);
    }
}
