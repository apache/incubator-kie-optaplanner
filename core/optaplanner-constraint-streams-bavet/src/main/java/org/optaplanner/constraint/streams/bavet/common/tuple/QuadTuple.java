
package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.core.impl.util.Quadruple;
import org.optaplanner.core.impl.util.Triple;

public interface QuadTuple<A, B, C, D> extends Tuple {

    static <A, B, C, D> QuadTuple<A, B, C, D> of(A a, B b, C c, D d, int storeSize) {
        switch (storeSize) {
            case 0:
                return new QuadStorelessTuple<>(a, b, c, d);
            case 1:
                return new QuadSingletonStoreTuple<>(a, b, c, d);
            default:
                return new QuadLargeStoreTuple<>(a, b, c, d, storeSize);
        }
    }

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

    C getC();

    void setC(C c);

    D getD();

    void setD(D d);

    default void fillFrom(Quadruple<A, B, C, D> quadruple) {
        setA(quadruple.getA());
        setB(quadruple.getB());
        setC(quadruple.getC());
        setD(quadruple.getD());
    }

    default void fillHeadFrom(TriTuple<A, B, C> tuple) {
        setA(tuple.getA());
        setB(tuple.getB());
        setC(tuple.getC());
    }

    default void fillTailFrom(Pair<C, D> pair) {
        setC(pair.getKey());
        setD(pair.getValue());
    }

    default void fillTailFrom(Triple<B, C, D> triple) {
        setB(triple.getA());
        setC(triple.getB());
        setD(triple.getC());
    }

}
