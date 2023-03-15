package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface UniTuple<A> extends Tuple {

    static <A> UniTuple<A> of(A a, int storeSize) {
        switch (storeSize) {
            case 0:
                return new UniStorelessTuple<>(a);
            case 1:
                return new UniSingletonStoreTuple<>(a);
            default:
                return new UniLargeStoreTuple<>(a, storeSize);
        }
    }

    A getA();

    void setA(A a);

}
