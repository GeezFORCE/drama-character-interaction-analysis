package restclients;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "processor")
@Dependent
public interface ProcessorRestClient {

    /**
     * REST Client Operation for processing a drama
     * @param drama , the name of the drama to be processed
     * @return REST accepted response from processor
     */
    @POST
    @Path("/process/{drama}")
    String process(@PathParam("drama") String drama);

}
