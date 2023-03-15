package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface BiTuple<A, B> extends Tuple {

    static <A, B> BiTuple<A, B> of(A a, B b, int storeSize) {
        switch (storeSize) {
            case 0:
                return new BiStorelessTuple<>(a, b);
            case 1:
                return new BiSingletonStoreTuple<>(a, b);
            default:
                return new BiLargeStoreTuple<>(a, b, storeSize);
        }
    }

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

}
