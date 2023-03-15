package org.optaplanner.constraint.streams.bavet.common.tuple;

public final class UniTupleImpl<A> extends AbstractTuple implements UniTuple<A> {

    private A a;

    UniTupleImpl(A a, int storeSize) {
        super(storeSize);
        this.a = a;
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
    public String toString() {
        return "{" + a + "}";
    }

}
