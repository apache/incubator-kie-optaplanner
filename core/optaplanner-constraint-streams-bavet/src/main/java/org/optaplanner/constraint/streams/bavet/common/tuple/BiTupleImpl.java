package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;

final class BiTupleImpl<A, B> extends AbstractTuple implements BiTuple<A, B> {

    private A a;
    private B b;

    BiTupleImpl(A a, B b, int storeSize) {
        super(storeSize);
        this.a = a;
        this.b = b;
    }

    @Override
    public A getA() {
        return a;
    }

    @Override
    public void setA(A a) {
        this.a = a;
    }

    @Override
    public B getB() {
        return b;
    }

    @Override
    public void setB(B b) {
        this.b = b;
    }

    @Override
    public void fillFrom(Pair<A, B> pair) {
        this.a = pair.getKey();
        this.b = pair.getValue();
    }

    @Override
    public String toString() {
        return "{" + a + ", " + b + "}";
    }

}
