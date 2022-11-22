package org.optaplanner.examples.common.persistence.jackson;

import org.optaplanner.examples.common.domain.AbstractPersistableJackson;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;

/**
 * Exists so that recursive data models (such as TSP chaining) can be serialized/deserialized using object references,
 * while at the same time being able to serialize/deserialize map keys using those same references.
 * (See Vehicle Routing example.)
 * <p>
 * For use cases without advanced referencing needs,
 * the less complex way of using {@link JsonIdentityInfo} with {@link PropertyGenerator} is preferred.
 * (See Cloud Balancing example.)
 * <p>
 * The implementation is similar in principle to {@link UUIDGenerator}, but without the long and undescriptive UUIDs.
 * Works only for children of {@link AbstractPersistableJackson}.
 * No two such classes must have the same {@link Class#getSimpleName()}.
 */
public final class JacksonUniqueIdGenerator extends ObjectIdGenerator<String> {

    private final Class<?> scope;

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
    public String generateId(Object forPojo) {
        return forPojo.getClass().getSimpleName() + "#" + ((AbstractPersistableJackson) forPojo).getId();
    }
}
