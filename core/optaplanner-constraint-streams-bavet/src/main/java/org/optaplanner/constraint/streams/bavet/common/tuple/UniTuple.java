package org.optaplanner.constraint.streams.bavet.common.tuple;

/**
 *
 * @param <A>
 * @implNote It is recommended for this interface to only ever have one implementation.
 *           In extensive benchmarks, we have seen significant performance drops coming from polymorphism here.
 *           Most notably as much as 20 % lowe score calculation counts in the Nurse Rostering example.
 */
public interface UniTuple<A> extends Tuple {

    static <A> UniTuple<A> of(A a, int storeSize) {
        return new UniTupleImpl<>(a, storeSize);
    }

    A getA();

    void setA(A a);

}
