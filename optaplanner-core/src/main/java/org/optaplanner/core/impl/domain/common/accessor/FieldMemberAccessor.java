/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.domain.common.accessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * A {@link MemberAccessor} based on a field.
 */
public final class FieldMemberAccessor implements MemberAccessor {

    private final Field field;

    public FieldMemberAccessor(Field field) {
        this.field = field;
        // Performance hack by avoiding security checks
        field.setAccessible(true);
    }

    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public Type getGenericType() {
        return field.getGenericType();
    }

    public Object executeGetter(Object bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot get the field (" + field.getName()
                    + ") on bean of class (" + bean.getClass() + ").", e);
        }
    }

    @Override
    public boolean supportSetter() {
        return true;
    }

    public void executeSetter(Object bean, Object value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot set the field (" + field.getName()
                    + ") on bean of class (" + bean.getClass() + ").", e);
        }
    }

    // ************************************************************************
    // AnnotatedElement methods
    // ************************************************************************

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return field.getDeclaredAnnotations();
    }

    @Override
    public String toString() {
        return "field " + field;
    }

}
