package org.optaplanner.constraint.streams.bavet.common.tuple;

import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.core.impl.util.Quadruple;
import org.optaplanner.core.impl.util.Triple;

final class QuadTupleImpl<A, B, C, D> extends AbstractTuple implements QuadTuple<A, B, C, D> {

    private A a;
    private B b;
    private C c;
    private D d;

    QuadTupleImpl(A a, B b, C c, D d, int storeSize) {
        super(storeSize);
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
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
    public D getD() {
        return d;
    }

    @Override
    public void setD(D d) {
        this.d = d;
    }

    @Override
    public void fillFrom(Quadruple<A, B, C, D> quadruple) {
        this.a = quadruple.getA();
        this.b = quadruple.getB();
        this.c = quadruple.getC();
        this.d = quadruple.getD();
    }

    @Override
    public void fillHeadFrom(TriTuple<A, B, C> tuple) {
        this.a = tuple.getA();
        this.b = tuple.getB();
        this.c = tuple.getC();
    }

    @Override
    public void fillTailFrom(Pair<C, D> pair) {
        this.c = pair.getKey();
        this.d = pair.getValue();
    }

    @Override
    public void fillTailFrom(Triple<B, C, D> triple) {
        this.b = triple.getA();
        this.c = triple.getB();
        this.d = triple.getC();
    }

    @Override
    public String toString() {
        return "{" + a + ", " + b + ", " + c + ", " + d + "}";
    }

}
