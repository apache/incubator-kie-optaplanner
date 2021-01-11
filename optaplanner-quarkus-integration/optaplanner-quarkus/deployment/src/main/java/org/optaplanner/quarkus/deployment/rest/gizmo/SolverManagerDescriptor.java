package org.optaplanner.quarkus.deployment.rest.gizmo;

import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;

import io.quarkus.gizmo.MethodDescriptor;

/**
 * The class provides {@link MethodDescriptor}s of a subset of the {@link SolverManager} interface's methods.
 */
public class SolverManagerDescriptor {

    public static MethodDescriptor getSolverStatus() {
        return MethodDescriptor
                .ofMethod(SolverManager.class, "getSolverStatus", SolverStatus.class, Object.class);
    }

    public static MethodDescriptor solveAndListen() {
        return MethodDescriptor
                .ofMethod(SolverManager.class, "solveAndListen", SolverJob.class, Object.class, Function.class, Consumer.class);
    }

    public static MethodDescriptor terminateEarly() {
        return MethodDescriptor
                .ofMethod(SolverManager.class, "terminateEarly", void.class, Object.class);
    }
}
