package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class ShortFieldCloner<C> implements FieldCloner<C> {

    private static final FieldCloner INSTANCE = new ShortFieldCloner();

    public static <C> FieldCloner<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass,
            C original, C clone) {
        short originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return Optional.empty();
    }

    private static short getFieldValue(Object bean, Field field) {
        try {
            return field.getShort(bean);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, short value) {
        try {
            field.setShort(bean, value);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

    private ShortFieldCloner() {

    }

}
