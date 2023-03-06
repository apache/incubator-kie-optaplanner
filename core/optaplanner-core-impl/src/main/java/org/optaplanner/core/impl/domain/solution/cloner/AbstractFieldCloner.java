package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Objects;

abstract class AbstractFieldCloner {

    protected Field field;

    protected AbstractFieldCloner(Field field) {
        this.field = Objects.requireNonNull(field);
    }

    protected static Object getGenericFieldValue(Object bean, Field field) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw createExceptionOnRead(bean, field, e);
        }
    }

    protected static RuntimeException createExceptionOnRead(Object bean, Field field, Exception rootCause) {
        return new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                + ") which cannot be read to create a planning clone.", rootCause);
    }

    protected static void setGenericFieldValue(Object bean, Field field, Object value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw createExceptionOnWrite(bean, field, value, e);
        }
    }

    protected static RuntimeException createExceptionOnWrite(Object bean, Field field, Object value, Exception rootCause) {
        return new IllegalStateException("The class (" + bean.getClass() + ") has a field (" + field
                + ") which cannot be written with the value (" + value + ") to create a planning clone.", rootCause);
    }

    /**
     * Reads field value from original and store it in clone.
     *
     * @param original never null
     * @param clone    never null
     * @return not null if the cloner decided not to clone
     * @throws RuntimeException if reflective field read or write fails
     */
    abstract <C> Unprocessed clone(C original, C clone);

}
