package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class LongFieldCloner extends AbstractFieldCloner {

    public LongFieldCloner(Field field) {
        super(field);
    }

    @Override
    public <C> Unprocessed clone(C original, C clone) {
        long originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return null;
    }

    private static long getFieldValue(Object bean, Field field) {
        try {
            return field.getLong(bean);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, long value) {
        try {
            field.setLong(bean, value);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

}
