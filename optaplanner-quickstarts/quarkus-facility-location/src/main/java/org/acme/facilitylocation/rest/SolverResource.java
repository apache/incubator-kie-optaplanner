package org.acme.facilitylocation.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;

@Path("/flp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolverResource {

    @Inject
    FacilityLocationProblemRepository repository;

    @GET
    @Path("get")
    public FacilityLocationProblem get() {
        return repository.solution().orElse(FacilityLocationProblem.empty());
    }
}
