package org.optaplanner.core.impl.util;

public interface Triple<A, B, C> {

    static <A, B, C> Triple<A, B, C> of(A a, B b, C c) {
        return new MutableTripleImpl<>(a, b, c);
    }

    A getA();

    B getB();

    C getC();

}
