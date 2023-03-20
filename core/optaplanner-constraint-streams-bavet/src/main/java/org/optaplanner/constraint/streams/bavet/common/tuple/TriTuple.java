package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.core.impl.util.Triple;

/**
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @implNote It is recommended for this interface to only ever have one implementation.
 *           In extensive benchmarks, we have seen significant performance drops coming from polymorphism here.
 *           Most notably as much as ~20 % drops in the Nurse Rostering example.
 */
public interface TriTuple<A, B, C> extends Tuple {

    static <A, B, C> TriTuple<A, B, C> of(A a, B b, C c, int storeSize) {
        return new TriTupleImpl<>(a, b, c, storeSize);
    }

    A getA();

    void setA(A a);

    B getB();

    void setB(B b);

    C getC();

    void setC(C c);

    void fillFrom(Triple<A, B, C> triple);

    void fillHeadFrom(BiTuple<A, B> tuple);

    void fillTailFrom(Pair<B, C> pair);

}
