package com.texttechnology.visualizer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Path("/api/graph")
@RequestScoped
public class CharacterGraphResource {

    private final CharacterGraphRepository repository;

    @Inject
    public CharacterGraphResource(CharacterGraphRepository repository) {
        this.repository = repository;
    }

    @GET
    @Path("/dramas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDramas() {
        try {
            List<String> dramas = repository.getAllDramaTitles();
            return Response.ok(dramas).build();
        } catch (Exception e) {
            log.error("Error fetching drama titles", e);
            return Response.serverError().entity("Error fetching drama titles: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/drama/{title}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDramaGraph(@PathParam("title") String title) {
        try {
            Map<String, Object> graph = repository.getCharacterInteractionGraph(title);
            return Response.ok(graph).build();
        } catch (Exception e) {
            log.error("Error fetching graph data for drama: " + title, e);
            return Response.serverError().entity("Error fetching graph data: " + e.getMessage()).build();
        }
    }
}