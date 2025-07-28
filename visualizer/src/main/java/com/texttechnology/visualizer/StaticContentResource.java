package com.texttechnology.visualizer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

@Path("/")
@RequestScoped
public class StaticContentResource {

    /**
     * @return response index.html
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndex() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("web/index.html");
        return Response.ok(is).build();
    }

    /**
     * @return response styles.css
     */
    @GET
    @Path("/styles.css")
    @Produces("text/css")
    public Response getStyles() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("web/styles.css");
        return Response.ok(is).build();
    }

    /**
     *
     * @return response visualizer script, responsible for drawing the graph in UI
     */
    @GET
    @Path("/visualizer.js")
    @Produces("application/javascript")
    public Response getScript() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("web/visualizer.js");
        return Response.ok(is).build();
    }
}