package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class DoubleFieldCloner<C> implements FieldCloner<C> {

    private static final FieldCloner INSTANCE = new DoubleFieldCloner();

    public static <C> FieldCloner<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass,
            C original, C clone) {
        double originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return Optional.empty();
    }

    private static double getFieldValue(Object bean, Field field) {
        try {
            return field.getDouble(bean);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnRead(bean, field, e);
        }
    }

    private static void setFieldValue(Object bean, Field field, double value) {
        try {
            field.setDouble(bean, value);
        } catch (IllegalAccessException e) {
            throw FieldCloner.createExceptionOnWrite(bean, field, value, e);
        }
    }

    private DoubleFieldCloner() {

    }

}
