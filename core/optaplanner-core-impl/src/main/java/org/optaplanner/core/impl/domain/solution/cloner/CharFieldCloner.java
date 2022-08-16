package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class CharFieldCloner<C> implements FieldCloner<C> {

    private static final FieldCloner INSTANCE = new CharFieldCloner();

    public static <C> FieldCloner<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instaceClass,
            C original, C clone) {
        char originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return Optional.empty();
    }

    private static char getFieldValue(Object bean, Field field) {
        try {
            return field.getChar(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                    + ") which cannot be read to create a planning clone.", e);
        }
    }

    private static void setFieldValue(Object bean, Field field, char value) {
        try {
            field.setChar(bean, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                    + ") which cannot be written with the value (" + value + ") to create a planning clone.", e);
        }
    }

    private CharFieldCloner() {

    }

}
