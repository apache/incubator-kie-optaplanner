package org.optaplanner.constraint.streams.bavet.common.tuple;

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
    public String toString() {
        return "{" + a + ", " + b + ", " + c + ", " + d + "}";
    }

}
