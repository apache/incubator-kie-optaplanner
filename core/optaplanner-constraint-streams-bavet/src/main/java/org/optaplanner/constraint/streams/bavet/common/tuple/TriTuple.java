package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface TriTuple<A, B, C> extends Tuple {

    static <A, B, C> TriTuple<A, B, C> of(A a, B b, C c, int storeSize) {
        switch (storeSize) {
            case 0:
                return new TriStorelessTuple<>(a, b, c);
            case 1:
                return new TriSingletonStoreTuple<>(a, b, c);
            default:
                return new TriLargeStoreTuple<>(a, b, c, storeSize);
        }
    }

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

    C getC();

    void setC(C c);

}
