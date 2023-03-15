
package org.optaplanner.constraint.streams.bavet.common.tuple;

public interface QuadTuple<A, B, C, D> extends Tuple {

    static <A, B, C, D> QuadTuple<A, B, C, D> of(A a, B b, C c, D d, int storeSize) {
        return new QuadTupleImpl<>(a, b, c, d, storeSize);
    }

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

    C getC();

    void setC(C c);

    D getD();

    void setD(D d);

}
