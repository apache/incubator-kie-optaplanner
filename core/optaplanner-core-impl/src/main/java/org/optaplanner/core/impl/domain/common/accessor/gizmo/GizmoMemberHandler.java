package org.optaplanner.core.impl.domain.common.accessor.gizmo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

interface GizmoMemberHandler {

    static GizmoMemberHandler of(Class<?> declaringClass, String name, Object memberDescriptor) {
        if (memberDescriptor instanceof FieldDescriptor) {
            try {
                Field field = declaringClass.getField(name);
                return new GizmoFieldHandler(declaringClass, field, (FieldDescriptor) memberDescriptor,
                        !Modifier.isFinal(field.getModifiers()));
            } catch (NoSuchFieldException e) { // The field is only used for its metadata and never actually called.
                return new GizmoFieldHandler(declaringClass, null, (FieldDescriptor) memberDescriptor, false);
            }
        } else if (memberDescriptor instanceof MethodDescriptor) {
            return new GizmoMethodHandler(declaringClass, (MethodDescriptor) memberDescriptor);
        } else {
            throw new IllegalStateException("Impossible state: memberDescriptor not " + FieldDescriptor.class.getSimpleName()
                    + " or " + MethodDescriptor.class.getSimpleName() + ".");
        }
    }

    void whenIsField(Consumer<FieldDescriptor> fieldDescriptorConsumer);

    void whenIsMethod(Consumer<MethodDescriptor> methodDescriptorConsumer);

    ResultHandle readMemberValue(BytecodeCreator bytecodeCreator, ResultHandle thisObj);

    boolean writeMemberValue(MethodDescriptor setter, BytecodeCreator bytecodeCreator, ResultHandle thisObj,
            ResultHandle newValue);

    String getDeclaringClassName();

    String getTypeName();

    Type getType();

}
