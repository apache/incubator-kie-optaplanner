package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class BooleanFieldCloner extends AbstractFieldCloner {

    public BooleanFieldCloner(Field field) {
        super(field);
    }

    @Override
    public <C> Unprocessed clone(C original, C clone) {
        boolean originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return null;
    }

    private static boolean getFieldValue(Object bean, Field field) {
        try {
            return field.getBoolean(bean);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, boolean value) {
        try {
            field.setBoolean(bean, value);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

}
