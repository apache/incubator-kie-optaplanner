package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class BooleanFieldCloner implements FieldCloner {

    static final FieldCloner INSTANCE = new BooleanFieldCloner();

    @Override
    public <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass, C original, C clone) {
        boolean originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return null;
    }

    private static boolean getFieldValue(Object bean, Field field) {
        try {
            return field.getBoolean(bean);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, boolean value) {
        try {
            field.setBoolean(bean, value);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

    private BooleanFieldCloner() {

    }

}
