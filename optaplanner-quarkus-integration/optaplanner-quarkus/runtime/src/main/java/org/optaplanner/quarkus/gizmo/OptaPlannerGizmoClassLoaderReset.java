package org.optaplanner.quarkus.gizmo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.optaplanner.core.impl.domain.common.accessor.gizmo.GizmoMemberAccessorImplementor;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class OptaPlannerGizmoClassLoaderReset {

    void onStart(@Observes StartupEvent ev) {
        GizmoMemberAccessorImplementor.resetClassLoaderCache();
    }

}
