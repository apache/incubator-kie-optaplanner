package org.optaplanner.constraint.streams.bavet.common.tuple;

public final class TriTupleImpl<A, B, C> extends AbstractTuple implements TriTuple<A, B, C> {

    private A a;
    private B b;
    private C c;

    public TriTupleImpl(A a, B b, C c, int storeSize) {
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
    public String toString() {
        return "{" + a + ", " + b + ", " + c + "}";
    }

}
