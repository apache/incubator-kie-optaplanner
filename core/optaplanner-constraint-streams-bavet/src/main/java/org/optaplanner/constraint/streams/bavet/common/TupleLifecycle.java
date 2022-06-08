package org.optaplanner.constraint.streams.bavet.common;

import java.util.function.Consumer;

public interface TupleLifecycle<Tuple_ extends Tuple> {

    static <Tuple_ extends Tuple> TupleLifecycle<Tuple_> of(Consumer<Tuple_> insert, Consumer<Tuple_> update,
            Consumer<Tuple_> retract) {
        return new TupleLifecycleImpl<>(insert, update, retract);
    }

    void insert(Tuple_ tuple);

    default void update(Tuple_ tuple) {
        retract(tuple);
        insert(tuple);
    }

    void retract(Tuple_ tuple);

}
