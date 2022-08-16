package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class IntFieldCloner<C> implements FieldCloner<C> {

    private static final FieldCloner INSTANCE = new IntFieldCloner();

    public static <C> FieldCloner<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instaceClass,
            C original, C clone) {
        int originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return Optional.empty();
    }

    private static int getFieldValue(Object bean, Field field) {
        try {
            return field.getInt(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                    + ") which cannot be read to create a planning clone.", e);
        }
    }

    private static void setFieldValue(Object bean, Field field, int value) {
        try {
            field.setInt(bean, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                    + ") which cannot be written with the value (" + value + ") to create a planning clone.", e);
        }
    }

    private IntFieldCloner() {

    }

}
