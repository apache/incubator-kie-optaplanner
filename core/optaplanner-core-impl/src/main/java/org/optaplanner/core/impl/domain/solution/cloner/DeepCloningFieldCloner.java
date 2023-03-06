package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class DeepCloningFieldCloner extends AbstractFieldCloner {

    private final DeepCloningUtils deepCloningUtils;

    public DeepCloningFieldCloner(Field field, DeepCloningUtils deepCloningUtils) {
        super(field);
        this.deepCloningUtils = deepCloningUtils;
    }

    @Override
    public <C> Unprocessed clone(C original, C clone) {
        Object originalValue = AbstractFieldCloner.getGenericFieldValue(original, field);
        if (isDeepCloneField(original.getClass(), originalValue)) {
            // Deffer filling in the field.
            return new Unprocessed(clone, field, originalValue);
        } else { // Shallow copy.
            AbstractFieldCloner.setGenericFieldValue(clone, field, originalValue);
            return null;
        }
    }

    private boolean isDeepCloneField(Class<?> fieldInstanceClass, Object originalValue) {
        if (originalValue == null) {
            return false;
        }
        return deepCloningUtils.getDeepCloneDecision(field, fieldInstanceClass, originalValue.getClass());
    }

}
