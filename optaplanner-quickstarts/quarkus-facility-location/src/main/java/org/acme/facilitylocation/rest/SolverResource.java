package org.acme.facilitylocation.rest;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.solver.SolverManager;

@Path("/flp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolverResource {

    private static final long PROBLEM_ID = 0L;

    @Inject
    FacilityLocationProblemRepository repository;
    @Inject
    SolverManager<FacilityLocationProblem, Long> solverManager;
    @Inject
    ScoreManager<FacilityLocationProblem> scoreManager;

    private Status status(FacilityLocationProblem solution) {
        return new Status(solution, scoreManager.explainScore(solution), solverManager.getSolverStatus(PROBLEM_ID));
    }

    @GET
    @Path("status")
    public Status status() {
        return status(repository.solution().orElse(FacilityLocationProblem.empty()));
    }

    @POST
    @Path("solve")
    public void solve() {
        Optional<FacilityLocationProblem> maybeSolution = repository.solution();
        maybeSolution.ifPresent(facilityLocationProblem -> solverManager.solveAndListen(
                PROBLEM_ID,
                id -> facilityLocationProblem,
                solution -> repository.update(solution)));
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}
