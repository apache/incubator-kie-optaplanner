package org.acme.facilitylocation.rest;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;
import org.optaplanner.core.api.solver.SolverManager;

@Path("/flp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolverResource {

    @Inject
    FacilityLocationProblemRepository repository;
    @Inject
    SolverManager<FacilityLocationProblem, Long> solverManager;

    @GET
    @Path("get")
    public FacilityLocationProblem get() throws ExecutionException, InterruptedException {
        Optional<FacilityLocationProblem> maybeSolution = repository.solution();
        if (maybeSolution.isPresent()) {
            FacilityLocationProblem solution = solverManager.solve(0L, maybeSolution.get()).getFinalBestSolution();
            repository.update(solution);
            return solution;
        }
        return FacilityLocationProblem.empty();
    }
}
