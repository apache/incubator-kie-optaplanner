package org.optaplanner.persistence.jackson.impl.domain.solution;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

/**
 * Similar in principle to {@link com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator},
 * but without the overly long UUIDs.
 */
public final class JacksonUniqueIdGenerator extends com.fasterxml.jackson.annotation.ObjectIdGenerator<String> {

    private final Class<?> scope;
    private BigInteger nextValue = BigInteger.ZERO;

    public JacksonUniqueIdGenerator() {
        this.scope = Object.class;
    }

    @Override
    public Class<?> getScope() {
        return scope;
    }

    @Override
    public boolean canUseFor(ObjectIdGenerator<?> gen) {
        return (gen.getClass() == getClass());
    }

    @Override
    public ObjectIdGenerator<String> forScope(Class<?> scope) {
        return this;
    }

    @Override
    public ObjectIdGenerator<String> newForSerialization(Object context) {
        return this;
    }

    @Override
    public IdKey key(Object key) {
        if (key == null) {
            return null;
        }
        return new IdKey(getClass(), null, key);
    }

    @Override
    public synchronized String generateId(Object forPojo) {
        BigInteger result = nextValue;
        nextValue = nextValue.add(BigInteger.ONE);
        return result.toString(16); // Shorten possibly large numbers to hexadecimal representation.
    }
}
