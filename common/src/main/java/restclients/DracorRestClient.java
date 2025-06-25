package restclients;

import jakarta.enterprise.context.Dependent;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/corpora")
@RegisterRestClient(configKey = "dracor-api")
@Dependent
public interface DracorRestClient {

    @GET
    @Path("/eng/plays/{play}/tei")
    @Produces(MediaType.APPLICATION_XML)
    String getTeiForPlay(@PathParam("play") String play);


    @GET
    @Path("/eng")
    @Produces(MediaType.APPLICATION_JSON)
    JsonObject getAllPlayDetails();
}