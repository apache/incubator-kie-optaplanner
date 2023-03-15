package org.optaplanner.constraint.streams.bavet.common.tuple;

final class UniSingletonStoreTuple<A> extends AbstractSingletonStoreTuple implements UniTuple<A> {

    private A a;

    UniSingletonStoreTuple(A a) {
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
