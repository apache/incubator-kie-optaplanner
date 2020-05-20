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

import org.acme.optaplanner.domain.Lesson;

import io.quarkus.panache.common.Sort;

@Path("/lessons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class LessonResource {

    @GET
    public List<Lesson> getAllLessons() {
        return Lesson.listAll(Sort.by("subject").and("teacher").and("studentGroup").and("id"));
    }

    @POST
    public Response add(Lesson lesson) {
        Lesson.persist(lesson);
        return Response.accepted(lesson).build();
    }

    @DELETE
    @Path("{lessonId}")
    public Response delete(@PathParam("lessonId") Long lessonId) {
        Lesson lesson = Lesson.findById(lessonId);
        if (lesson == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        lesson.delete();
        return Response.status(Response.Status.OK).build();
    }

}
