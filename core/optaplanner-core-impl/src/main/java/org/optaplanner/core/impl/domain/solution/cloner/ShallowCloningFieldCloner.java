package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class ShallowCloningFieldCloner implements FieldCloner {

    static final FieldCloner INSTANCE = new ShallowCloningFieldCloner();

    @Override
    public <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass, C original, C clone) {
        Object originalValue = FieldCloner.getFieldValue(original, field);
        FieldCloner.setFieldValue(clone, field, originalValue);
        return null;
    }

    private ShallowCloningFieldCloner() {

    }

}
