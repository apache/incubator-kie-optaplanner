package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

@FunctionalInterface
interface FieldCloner<C> {

    static Object getFieldValue(Object bean, Field field) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw createExceptionOnRead(bean, field, e);
        }
    }

    static RuntimeException createExceptionOnRead(Object bean, Field field, Exception rootCause) {
        return new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                + ") which cannot be read to create a planning clone.", rootCause);
    }

    static void setFieldValue(Object bean, Field field, Object value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw createExceptionOnWrite(bean, field, value, e);
        }
    }

    static RuntimeException createExceptionOnWrite(Object bean, Field field, Object value, Exception rootCause) {
        return new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                + ") which cannot be written with the value (" + value + ") to create a planning clone.", rootCause);
    }

    Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass, C original,
            C clone);

}
