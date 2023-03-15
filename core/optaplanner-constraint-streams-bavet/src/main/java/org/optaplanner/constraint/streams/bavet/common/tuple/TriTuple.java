package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.core.impl.util.Triple;

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

    default void fillFrom(Triple<A, B, C> triple) {
        setA(triple.getA());
        setB(triple.getB());
        setC(triple.getC());
    }

    default void fillHeadFrom(BiTuple<A, B> tuple) {
        setA(tuple.getA());
        setB(tuple.getB());
    }

    default void fillTailFrom(Pair<B, C> pair) {
        setB(pair.getKey());
        setC(pair.getValue());
    }

}
