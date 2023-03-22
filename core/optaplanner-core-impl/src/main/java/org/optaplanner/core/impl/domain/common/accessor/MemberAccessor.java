package org.optaplanner.core.impl.domain.common.accessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Fast and easy access to a {@link Member} of a bean,
 * which is a property (with a getter and optional setter {@link Method}) or a {@link Field}.
 */
public interface MemberAccessor {

    Class<?> getDeclaringClass();

    String getName();

    Class<?> getType();

    /**
     * As defined by {@link Method#getGenericReturnType()} and {@link Field#getGenericType()}.
     *
     * @return never null
     */
    Type getGenericType();

    Object executeGetter(Object bean);

    boolean supportSetter();

    void executeSetter(Object bean, Object value);

    String getSpeedNote();

    /**
     * As defined in {@link AnnotatedElement#getAnnotation(Class)}.
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass);

}
