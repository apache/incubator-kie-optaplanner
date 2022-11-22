package org.optaplanner.examples.common.persistence.jackson;

import java.util.Objects;

import org.optaplanner.examples.common.domain.AbstractPersistableJackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * Deserializes map key defined by {@link JacksonUniqueIdGenerator} to a child of {@link AbstractPersistableJackson}.
 * <p>
 * Deserialization will create new instances of the map key type.
 * Duplicate instances will be created if any other part of the JSON is also referencing the same type.
 * In that case, a custom implementation of {@link AbstractExampleSolutionFileIO} must be used later
 * to resolve the duplicates by comparing IDs of such objects and making sure only one instance exists with each ID.
 *
 * @param <E> The type must have a {@link com.fasterxml.jackson.annotation.JsonIdentityInfo} annotation with
 *        {@link JacksonUniqueIdGenerator} as its generator.
 */
public abstract class AbstractKeyDeserializer<E extends AbstractPersistableJackson> extends KeyDeserializer {

    private final Class<E> persistableClass;

    protected AbstractKeyDeserializer(Class<E> persistableClass) {
        this.persistableClass = Objects.requireNonNull(persistableClass);
    }

    @Override
    public final E deserializeKey(String value, DeserializationContext deserializationContext) {
        String[] parts = value.split("#");
        String className = parts[0];
        if (!Objects.equals(className, persistableClass.getSimpleName())) {
            throw new IllegalStateException("Impossible state: not the correct type (" + value + ").");
        }
        String idString = parts[1];
        try {
            long id = Long.parseLong(idString);
            return createInstance(id); // Need to be de-duplicated in solution IO.
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Impossible state: id is not a number (" + idString + ")");
        }
    }

    protected abstract E createInstance(long id);

}
