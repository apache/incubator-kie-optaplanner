package org.optaplanner.core.impl.domain.common.accessor.gizmo;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

public interface GizmoMemberHandler {

    static GizmoMemberHandler of(Object memberDescriptor) {
        if (memberDescriptor instanceof FieldDescriptor) {
            return new GizmoFieldHandler((FieldDescriptor) memberDescriptor);
        } else if (memberDescriptor instanceof MethodDescriptor) {
            return new GizmoMethodHandler((MethodDescriptor) memberDescriptor);
        } else {
            throw new IllegalStateException("Impossible state: memberDescriptor not " + FieldDescriptor.class.getSimpleName()
                    + " or " + MethodDescriptor.class.getSimpleName() + ".");
        }
    }

    void whenIsField(Consumer<FieldDescriptor> fieldDescriptorConsumer);

    void whenIsMethod(Consumer<MethodDescriptor> methodDescriptorConsumer);

    ResultHandle readMemberValue(Class<?> declaringClass, BytecodeCreator bytecodeCreator, ResultHandle thisObj);

    boolean writeMemberValue(Class<?> declaringClass, String name, MethodDescriptor setter, BytecodeCreator bytecodeCreator,
            ResultHandle thisObj, ResultHandle newValue, boolean ignoreFinalChecks);

    String getDeclaringClassName();

    String getTypeName();

    Type getType(Class<?> declaringClass);

}
