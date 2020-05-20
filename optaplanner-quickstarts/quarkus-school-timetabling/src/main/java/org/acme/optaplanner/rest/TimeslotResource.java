package org.acme.optaplanner.rest;

import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.optaplanner.domain.Timeslot;

import io.quarkus.panache.common.Sort;

@Path("/timeslots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class TimeslotResource {

    @GET
    public List<Timeslot> getAllTimeslots() {
        return Timeslot.listAll(Sort.by("dayOfWeek").and("startTime").and("endTime").and("id"));
    }

    @POST
    public Response add(Timeslot timeslot) {
        Timeslot.persist(timeslot);
        return Response.accepted(timeslot).build();
    }

    @DELETE
    @Path("{timeslotId}")
    public Response delete(@PathParam("timeslotId") Long timeslotId) {
        Timeslot timeslot = Timeslot.findById(timeslotId);
        if (timeslot == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        timeslot.delete();
        return Response.status(Response.Status.OK).build();
    }

}
