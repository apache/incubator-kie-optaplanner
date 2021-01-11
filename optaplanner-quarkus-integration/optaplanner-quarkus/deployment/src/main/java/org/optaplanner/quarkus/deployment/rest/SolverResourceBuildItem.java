package org.optaplanner.quarkus.deployment.rest;

import io.quarkus.builder.item.MultiBuildItem;

public final class SolverResourceBuildItem extends MultiBuildItem {

    private final SolverResourceInfo solverResourceInfo;

    public SolverResourceBuildItem(SolverResourceInfo solverResourceInfo) {
        this.solverResourceInfo = solverResourceInfo;
    }

    public SolverResourceInfo getSolverResourceInfo() {
        return solverResourceInfo;
    }
}
