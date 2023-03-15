package org.optaplanner.constraint.streams.bavet.common.tuple;

public final class BiTupleImpl<A, B> extends AbstractTuple implements BiTuple<A, B> {

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
    public String toString() {
        return "{" + a + ", " + b + "}";
    }

}
