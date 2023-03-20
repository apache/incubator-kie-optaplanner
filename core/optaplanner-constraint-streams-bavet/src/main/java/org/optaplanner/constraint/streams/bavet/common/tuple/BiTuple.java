package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;

/**
 *
 * @param <A>
 * @param <B>
 * @implNote It is recommended for this interface to only ever have one implementation.
 *           In extensive benchmarks, we have seen significant performance drops coming from polymorphism here.
 *           Most notably as much as ~20 % drops in the Nurse Rostering example.
 */
public interface BiTuple<A, B> extends Tuple {

    static <A, B> BiTuple<A, B> of(A a, B b, int storeSize) {
        return new BiTupleImpl<>(a, b, storeSize);
    }

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

    void fillFrom(Pair<A, B> pair);

}
