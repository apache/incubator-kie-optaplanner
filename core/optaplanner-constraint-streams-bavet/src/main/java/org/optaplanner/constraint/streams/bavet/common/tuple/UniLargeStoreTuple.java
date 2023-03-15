package org.optaplanner.constraint.streams.bavet.common.tuple;

final class UniLargeStoreTuple<A> extends AbstractLargeStoreTuple implements UniTuple<A> {

    private A a;

    UniLargeStoreTuple(A a, int storeSize) {
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
