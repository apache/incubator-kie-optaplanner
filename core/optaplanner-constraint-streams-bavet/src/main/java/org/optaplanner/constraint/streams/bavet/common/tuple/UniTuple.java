package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface UniTuple<A> extends Tuple {

    static <A> UniTuple<A> of(A a, int storeSize) {
        return new UniTupleImpl<>(a, storeSize);
    }

    A getA();

    void setA(A a);

}
