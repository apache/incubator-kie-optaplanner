package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.core.impl.util.Triple;

final class TriTupleImpl<A, B, C> extends AbstractTuple implements TriTuple<A, B, C> {

    private A a;
    private B b;
    private C c;

    TriTupleImpl(A a, B b, C c, int storeSize) {
        super(storeSize);
        this.a = a;
        this.b = b;
        this.c = c;
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
    public C getC() {
        return c;
    }

    @Override
    public void setC(C c) {
        this.c = c;
    }

    @Override
    public void fillFrom(Triple<A, B, C> triple) {
        this.a = triple.getA();
        this.b = triple.getB();
        this.c = triple.getC();
    }

    @Override
    public void fillHeadFrom(BiTuple<A, B> tuple) {
        this.a = tuple.getA();
        this.b = tuple.getB();
    }

    @Override
    public void fillTailFrom(Pair<B, C> pair) {
        this.b = pair.getKey();
        this.c = pair.getValue();
    }

    @Override
    public String toString() {
        return "{" + a + ", " + b + ", " + c + "}";
    }

}
