package org.optaplanner.quarkus.deployment.rest.gizmo;

import org.optaplanner.quarkus.remote.repository.SolutionRepository;

import io.quarkus.gizmo.MethodDescriptor;

/**
 * The class provides {@link MethodDescriptor}s of the {@link SolutionRepository} interface's methods.
 */
public class SolutionRepositoryDescriptor {

    public static MethodDescriptor save() {
        return MethodDescriptor
                .ofMethod(SolutionRepository.class, "save", void.class, Object.class, Object.class);
    }

    public static MethodDescriptor load() {
        return MethodDescriptor.ofMethod(SolutionRepository.class, "load", Object.class, Object.class);
    }
}
