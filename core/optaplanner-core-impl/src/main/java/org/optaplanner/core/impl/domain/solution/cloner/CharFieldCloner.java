package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class CharFieldCloner<C> implements FieldCloner<C> {

    private static final FieldCloner INSTANCE = new CharFieldCloner();

    public static <C> FieldCloner<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass,
            C original, C clone) {
        char originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return Optional.empty();
    }

    private static char getFieldValue(Object bean, Field field) {
        try {
            return field.getChar(bean);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, char value) {
        try {
            field.setChar(bean, value);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

    private CharFieldCloner() {

    }

}
