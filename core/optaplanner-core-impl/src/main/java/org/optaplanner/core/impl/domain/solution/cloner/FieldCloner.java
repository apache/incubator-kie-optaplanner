package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

@FunctionalInterface
interface FieldCloner {

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

    /**
     * Reads field value from original and store it in clone.
     *
     * @param deepCloningUtils never null
     * @param field never null
     * @param instanceClass never null
     * @param original never null
     * @param clone never null
     * @return not null if the cloner decided not to clone
     * @throws RuntimeException if reflective field read or write fails
     */
    <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass, C original, C clone);

}
