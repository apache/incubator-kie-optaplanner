package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface TriTuple<A, B, C> extends Tuple {

    A getFactA();

    B getFactB();

    C getFactC();

}
