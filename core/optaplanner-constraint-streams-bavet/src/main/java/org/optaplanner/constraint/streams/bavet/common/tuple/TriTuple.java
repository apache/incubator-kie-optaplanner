package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface TriTuple<A, B, C> extends Tuple {

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

    C getC();

    void setC(C c);

}
