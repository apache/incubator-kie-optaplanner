package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class IntFieldCloner extends AbstractFieldCloner {

    public IntFieldCloner(Field field) {
        super(field);
    }

    @Override
    public <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, C original, C clone) {
        int originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return null;
    }

    private static int getFieldValue(Object bean, Field field) {
        try {
            return field.getInt(bean);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, int value) {
        try {
            field.setInt(bean, value);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

}
