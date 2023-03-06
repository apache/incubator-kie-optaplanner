package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class ByteFieldCloner extends AbstractFieldCloner {

    public ByteFieldCloner(Field field) {
        super(field);
    }

    @Override
    public <C> Unprocessed clone(C original, C clone) {
        byte originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return null;
    }

    private static byte getFieldValue(Object bean, Field field) {
        try {
            return field.getByte(bean);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, byte value) {
        try {
            field.setByte(bean, value);
        } catch (IllegalAccessException e) {
            throw AbstractFieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

}
