package org.optaplanner.core.impl.domain.common.accessor.gizmo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

final class GizmoFieldHandler implements GizmoMemberHandler {

    private final FieldDescriptor fieldDescriptor;

    GizmoFieldHandler(FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    @Override
    public void whenIsField(Consumer<FieldDescriptor> fieldDescriptorConsumer) {
        fieldDescriptorConsumer.accept(fieldDescriptor);
    }

    @Override
    public void whenIsMethod(Consumer<MethodDescriptor> methodDescriptorConsumer) {
        // Do nothing.
    }

    @Override
    public ResultHandle readMemberValue(Class<?> declaringClass, BytecodeCreator bytecodeCreator, ResultHandle thisObj) {
        return bytecodeCreator.readInstanceField(fieldDescriptor, thisObj);
    }

    @Override
    public boolean writeMemberValue(Class<?> declaringClass, String name, MethodDescriptor setter,
            BytecodeCreator bytecodeCreator, ResultHandle thisObj, ResultHandle newValue, boolean ignoreFinalChecks) {
        try {
            Field field = declaringClass.getField(name);
            if (!ignoreFinalChecks && Modifier.isFinal(field.getModifiers())) {
                throw new IllegalStateException(
                        "Field (" + name + ") of class (" + declaringClass + ") is final and cannot be modified.");
            } else {
                bytecodeCreator.writeInstanceField(fieldDescriptor, thisObj, newValue);
                return true;
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Field (" + name + ") of class (" + declaringClass + ") does not exist.", e);
        }
    }

    @Override
    public String getDeclaringClassName() {
        return fieldDescriptor.getDeclaringClass();
    }

    @Override
    public String getTypeName() {
        return fieldDescriptor.getType();
    }

    @Override
    public Type getType(Class<?> declaringClass) {
        try {
            return declaringClass.getDeclaredField(fieldDescriptor.getName()).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                    "Cannot find field (" + fieldDescriptor.getName() + ") on class (" + declaringClass + ").",
                    e);
        }
    }

    @Override
    public String toString() {
        return fieldDescriptor.toString();
    }

}
