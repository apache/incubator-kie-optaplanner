package org.optaplanner.core.impl.domain.solution.cloner;

import org.optaplanner.core.api.function.TriPredicate;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

final class DeepCloningFieldCloner<C> implements FieldCloner<C> {
    private final TriPredicate<Field, Class<? extends C>, Object> isDeepCloned;

    public DeepCloningFieldCloner(TriPredicate<Field, Class<? extends C>, Object> isDeepCloned) {
        this.isDeepCloned = Objects.requireNonNull(isDeepCloned);
    }

    @Override
    public Optional<Unprocessed> clone(Field field, Class<? extends C> instanceClass, C original, C clone) {
        Object originalValue = FieldCloner.getFieldValue(original, field);
        if (isDeepCloned.test(field, instanceClass, originalValue)) { // Postpone filling in the fields.
            return Optional.of(new Unprocessed(clone, field, originalValue));
        } else { // Shallow copy.
            FieldCloner.setFieldValue(clone, field, originalValue);
            return Optional.empty();
        }
    }
}
