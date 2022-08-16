package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class DeepCloningFieldCloner<C> implements FieldCloner<C> {

    @Override
    public Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass,
            C original, C clone) {
        Object originalValue = FieldCloner.getFieldValue(original, field);
        if (isDeepCloneField(deepCloningUtils, field, instanceClass, originalValue)) { // Postpone filling in the fields.
            return Optional.of(new Unprocessed(clone, field, originalValue));
        } else { // Shallow copy.
            FieldCloner.setFieldValue(clone, field, originalValue);
            return Optional.empty();
        }
    }

    private static boolean isDeepCloneField(DeepCloningUtils deepCloningUtils, Field field, Class<?> fieldInstanceClass,
            Object originalValue) {
        if (originalValue == null) {
            return false;
        }
        return deepCloningUtils.getDeepCloneDecision(field, fieldInstanceClass,
                originalValue.getClass());
    }
}
