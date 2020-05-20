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

import org.acme.optaplanner.domain.Room;

import io.quarkus.panache.common.Sort;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class RoomResource {

    @GET
    public List<Room> getAllRooms() {
        return Room.listAll(Sort.by("name").and("id"));
    }

    // To try:  curl -d '{"name":"Room Z"}' -H "Content-Type: application/json" -X POST http://localhost:8080/rooms
    @POST
    public Response add(Room room) {
        Room.persist(room);
        return Response.accepted(room).build();
    }

    @DELETE
    @Path("{roomId}")
    public Response delete(@PathParam("roomId") Long roomId) {
        Room room = Room.findById(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        room.delete();
        return Response.status(Response.Status.OK).build();
    }

}
