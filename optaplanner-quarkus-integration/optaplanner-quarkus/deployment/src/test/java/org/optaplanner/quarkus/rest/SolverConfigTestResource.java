package org.optaplanner.quarkus.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.optaplanner.core.config.solver.SolverConfig;

@Path("/solver-config")
@ApplicationScoped
public class SolverConfigTestResource {

    @Inject
    SolverConfig solverConfig;

    @GET
    @Path("/seconds-spent-limit")
    @Produces(MediaType.TEXT_PLAIN)
    public String secondsSpentLimit() {
        return "secondsSpentLimit=" + solverConfig.getTerminationConfig().getSecondsSpentLimit();
    }

}
