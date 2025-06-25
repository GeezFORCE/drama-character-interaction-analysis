package restclients;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Path("exist/rest/db")
@RegisterRestClient(configKey = "exist-db")
@Dependent
public interface ExistDbRestClient {

    @PUT
    @Path("/plays/{playName}.xml")
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    Response insertPlay(@PathParam("playName") String playName, String tei);

    @GET
    @Path("/plays/{playName}.xml")
    @Produces(MediaType.APPLICATION_XML)
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    String getPlayXML(@PathParam("playName") String playName);

    @GET
    @Path("/plays")
    @Produces(MediaType.APPLICATION_XML)
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    String getAllInsertedPlays();

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    String xquery(String query);


    default String generateAuthHeader() {
        return "Basic " + Base64.getEncoder().encodeToString("admin:".getBytes(StandardCharsets.UTF_8));
    }

}