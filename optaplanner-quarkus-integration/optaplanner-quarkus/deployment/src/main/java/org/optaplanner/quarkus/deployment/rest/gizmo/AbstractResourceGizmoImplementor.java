package org.optaplanner.quarkus.deployment.rest.gizmo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import io.quarkus.gizmo.AnnotatedElement;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.ResultHandle;

abstract class AbstractResourceGizmoImplementor {

    private static final int ACC_PACKAGE_PRIVATE = 0x0000;

    private final Map<Class<?>, FieldDescriptor> implementedFields = new HashMap<>();

    protected void injectBean(ClassCreator classCreator, Class<?> type, String fieldName, int modifiers) {
        FieldCreator fieldCreator = classCreator.getFieldCreator(fieldName, type);
        fieldCreator.setModifiers(modifiers);
        fieldCreator.addAnnotation(Inject.class);
        implementedFields.put(type, fieldCreator.getFieldDescriptor());
    }

    protected void injectBean(ClassCreator classCreator, Class<?> type) {
        injectBean(classCreator, type, defaultFieldName(type), ACC_PACKAGE_PRIVATE);
    }

    private String defaultFieldName(Class<?> type) {
        char[] simpleNameChars = type.getSimpleName().toCharArray();
        simpleNameChars[0] = Character.toLowerCase(simpleNameChars[0]);
        return String.valueOf(simpleNameChars);
    }

    protected FieldDescriptor getImplementedFieldByType(Class<?> type) {
        Objects.requireNonNull(type);
        FieldDescriptor fieldDescriptor = implementedFields.get(type);
        if (fieldDescriptor == null) {
            throw new IllegalArgumentException("No field of the type (" + type.getName() + ") has been implemented.");
        }
        return fieldDescriptor;
    }

    protected ResultHandle getImplementedFieldInstanceByType(BytecodeCreator bytecodeCreator, Class<?> type,
            ResultHandle owner) {
        return bytecodeCreator.readInstanceField(getImplementedFieldByType(type), owner);
    }

    protected ResultHandle getImplementedFieldInstanceByType(BytecodeCreator bytecodeCreator, Class<?> type) {
        return getImplementedFieldInstanceByType(bytecodeCreator, type, bytecodeCreator.getThis());
    }

    protected void addPathAnnotation(AnnotatedElement annotatedElement, String path) {
        annotatedElement
                .addAnnotation(Path.class)
                .addValue("value", path);
    }

    protected void addConsumesAnnotation(AnnotatedElement annotatedElement, String... mediaTypes) {
        annotatedElement
                .addAnnotation(Consumes.class)
                .addValue("value", mediaTypes);
    }

    protected void addProducesAnnotation(AnnotatedElement annotatedElement, String... mediaTypes) {
        annotatedElement
                .addAnnotation(Produces.class)
                .addValue("value", mediaTypes);
    }

    protected void addGETAnnotation(AnnotatedElement annotatedElement) {
        annotatedElement.addAnnotation(GET.class);
    }

    protected void addPOSTAnnotation(AnnotatedElement annotatedElement) {
        annotatedElement.addAnnotation(POST.class);
    }

    protected void addPathParamAnnotation(AnnotatedElement annotatedElement, String pathParam) {
        annotatedElement
                .addAnnotation(PathParam.class)
                .addValue("value", pathParam);
    }
}
