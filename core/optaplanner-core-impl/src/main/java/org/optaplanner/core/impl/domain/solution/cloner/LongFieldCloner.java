package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class LongFieldCloner implements FieldCloner {

    static final FieldCloner INSTANCE = new LongFieldCloner();

    @Override
    public <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass, C original, C clone) {
        long originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return null;
    }

    private static long getFieldValue(Object bean, Field field) {
        try {
            return field.getLong(bean);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, long value) {
        try {
            field.setLong(bean, value);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

    private LongFieldCloner() {

    }

}
