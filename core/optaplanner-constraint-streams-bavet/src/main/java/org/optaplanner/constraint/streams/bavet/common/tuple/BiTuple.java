package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface BiTuple<A, B> extends Tuple {

    A getFactA();

    B getFactB();

}
