package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class CharFieldCloner extends AbstractFieldCloner {

    public CharFieldCloner(Field field) {
        super(field);
    }

    @Override
    public <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, C original, C clone) {
        char originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return null;
    }

    private static char getFieldValue(Object bean, Field field) {
        try {
            return field.getChar(bean);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, char value) {
        try {
            field.setChar(bean, value);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

}
