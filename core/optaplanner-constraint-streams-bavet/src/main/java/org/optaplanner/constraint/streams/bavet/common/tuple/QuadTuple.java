
package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.core.impl.util.Quadruple;
import org.optaplanner.core.impl.util.Triple;

/**
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
 * @implNote It is recommended for this interface to only ever have one implementation.
 *           In extensive benchmarks, we have seen significant performance drops coming from polymorphism here.
 *           Most notably as much as ~20 % drops in the Nurse Rostering example.
 */
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

    void fillFrom(Quadruple<A, B, C, D> quadruple);

    void fillHeadFrom(TriTuple<A, B, C> tuple);

    void fillTailFrom(Pair<C, D> pair);

    void fillTailFrom(Triple<B, C, D> triple);

}
