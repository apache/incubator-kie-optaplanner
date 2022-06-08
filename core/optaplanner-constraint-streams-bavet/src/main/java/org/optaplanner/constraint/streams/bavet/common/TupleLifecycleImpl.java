package org.optaplanner.constraint.streams.bavet.common;

import java.util.Objects;
import java.util.function.Consumer;

final class TupleLifecycleImpl<Tuple_ extends Tuple>
    implements TupleLifecycle<Tuple_> {

    private final Consumer<Tuple_> insert;
    private final Consumer<Tuple_> update;
    private final Consumer<Tuple_> retract;

    TupleLifecycleImpl(Consumer<Tuple_> insert, Consumer<Tuple_> update, Consumer<Tuple_> retract) {
        this.insert = Objects.requireNonNull(insert);
        this.update = Objects.requireNonNull(update);
        this.retract = Objects.requireNonNull(retract);
    }

    @Override
    public void insert(Tuple_ tuple) {
        insert.accept(tuple);
    }

    @Override
    public void update(Tuple_ tuple) {
        update.accept(tuple);
    }

    @Override
    public void retract(Tuple_ tuple) {
        retract.accept(tuple);
    }
}
