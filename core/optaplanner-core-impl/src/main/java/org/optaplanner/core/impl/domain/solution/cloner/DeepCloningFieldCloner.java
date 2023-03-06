package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class DeepCloningFieldCloner implements FieldCloner {

    static final FieldCloner INSTANCE = new DeepCloningFieldCloner();

    @Override
    public <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass, C original,
            C clone) {
        Object originalValue = FieldCloner.getFieldValue(original, field);
        if (isDeepCloneField(deepCloningUtils, field, instanceClass, originalValue)) { // Deffer filling in the field.
            return new Unprocessed(clone, field, originalValue);
        } else { // Shallow copy.
            FieldCloner.setFieldValue(clone, field, originalValue);
            return null;
        }
    }

    private static boolean isDeepCloneField(DeepCloningUtils deepCloningUtils, Field field, Class<?> fieldInstanceClass,
            Object originalValue) {
        if (originalValue == null) {
            return false;
        }
        return deepCloningUtils.getDeepCloneDecision(field, fieldInstanceClass, originalValue.getClass());
    }

    private DeepCloningFieldCloner() {

    }
}
