package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface BiTuple<A, B> extends Tuple {

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

}
