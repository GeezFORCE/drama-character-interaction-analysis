package restclients;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "processor")
@Dependent
public interface ProcessorRestClient {
    @POST
    @Path("/process/{drama}")
    String process(@PathParam("drama") String drama);

}
