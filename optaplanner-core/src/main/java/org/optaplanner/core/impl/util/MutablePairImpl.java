package org.optaplanner.core.impl.util;

import java.util.Objects;

final class MutablePairImpl<A, B> implements MutablePair<A, B> {

    private A key;
    private B value;

    public MutablePairImpl(A key, B value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public MutablePair<A, B> setKey(A key) {
        this.key = key;
        return this;
    }

    @Override
    public MutablePair<A, B> setValue(B value) {
        this.value = value;
        return this;
    }

    @Override
    public A getKey() {
        return key;
    }

    @Override
    public B getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MutablePairImpl<?, ?> that = (MutablePairImpl<?, ?>) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "(" + key + ", " + value + ")";
    }
}
