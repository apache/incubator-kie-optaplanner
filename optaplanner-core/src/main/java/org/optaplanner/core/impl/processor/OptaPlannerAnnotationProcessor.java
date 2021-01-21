package org.optaplanner.core.impl.processor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

public class OptaPlannerAnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("org.optaplanner.core.api.*");
    }

    private static final List<String> planningEntityMemberAnnotationList = Arrays.asList(
            PlanningVariable.class.getSimpleName(),
            InverseRelationShadowVariable.class.getSimpleName(),
            AnchorShadowVariable.class.getSimpleName(),
            CustomShadowVariable.class.getSimpleName());

    private HashSet<String> planningEntityClassList;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        planningEntityClassList = new HashSet<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            if (planningEntityMemberAnnotationList
                    .stream()
                    .anyMatch(annotationName -> annotation.getSimpleName().contentEquals(annotationName))) {
                processPlanningEntityMemberAnnotation(annotation, roundEnv);
            }
        }

        return true;
    }

    private void processPlanningEntityMemberAnnotation(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
        annotatedElements.forEach(element -> {
            switch (element.getKind()) {
                case FIELD:
                case METHOD:
                    if (element.getEnclosingElement().getAnnotation(PlanningEntity.class) == null) {
                        throw new IllegalStateException("Member annotation @" + annotation.getSimpleName() +
                                " was placed in a class without @PlanningEntity ("
                                + element.getEnclosingElement().getSimpleName()
                                + "). Maybe add @PlanningEntity to the class (" + element.getEnclosingElement().getSimpleName()
                                + ")?");
                    }
                    break;
                default:
                    throw new IllegalStateException("Member annotation @" + annotation.getSimpleName() +
                            " was placed on a non-member " + element + ".");
            }
        });
    }

}
