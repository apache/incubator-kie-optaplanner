package org.optaplanner.constraint.streams.bavet.common;

public interface TupleLifecycle<Tuple_ extends Tuple> {

    void insert(Tuple_ tuple);

    default void update(Tuple_ tuple) {
        retract(tuple);
        insert(tuple);
    }

    void retract(Tuple_ tuple);

}
