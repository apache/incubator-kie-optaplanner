package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface BiTuple<A, B> extends Tuple {

    static <A, B> BiTuple<A, B> of(A a, B b, int storeSize) {
        return new BiTupleImpl<>(a, b, storeSize);
    }

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

}
