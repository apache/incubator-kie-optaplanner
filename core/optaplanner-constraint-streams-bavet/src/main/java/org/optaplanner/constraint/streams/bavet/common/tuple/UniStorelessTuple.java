package org.optaplanner.constraint.streams.bavet.common.tuple;

final class UniStorelessTuple<A> extends AbstractStorelessTuple implements UniTuple<A> {

    private A a;

    UniStorelessTuple(A a) {
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
