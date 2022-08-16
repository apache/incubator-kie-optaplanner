package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

@FunctionalInterface
interface FieldCloner<C> {

    static Object getFieldValue(Object bean, Field field) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            failOnRead(bean, field, e);
            return null;
        }
    }

    static void failOnRead(Object bean, Field field, Exception rootCause) {
        throw new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                + ") which cannot be read to create a planning clone.", rootCause);
    }

    static void setFieldValue(Object bean, Field field, Object value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            failOnWrite(bean, field, value, e);
        }
    }

    static void failOnWrite(Object bean, Field field, Object value, Exception rootCause) {
        throw new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                + ") which cannot be written with the value (" + value + ") to create a planning clone.", rootCause);
    }

    Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass, C original,
            C clone);

}
