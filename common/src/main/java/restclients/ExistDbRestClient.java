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

    /**
     * REST API Client operation to insert the play content to the play collection in eXist-DB
     *
     * @param playName Name of the play
     * @param tei      TEI XML String
     * @return REST API Response received from eXist-DB
     */
    @PUT
    @Path("/plays/{playName}.xml")
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    Response insertPlay(@PathParam("playName") String playName, String tei);

    /**
     * REST API Client operation to get the XML content of a play
     *
     * @param playName Name of the play
     * @return REST API Response containing the content
     */
    @GET
    @Path("/plays/{playName}.xml")
    @Produces(MediaType.APPLICATION_XML)
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    String getPlayXML(@PathParam("playName") String playName);

    /**
     * REST API Client operation to get all the plays inserted in eXist-DB
     *
     * @return REST API Response containing the list of plays inserted
     */
    @GET
    @Path("/plays")
    @Produces(MediaType.APPLICATION_XML)
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    String getAllInsertedPlays();

    /**
     * REST API Client operation to run a custom XQuery
     *
     * @param query XQuery
     * @return REST API Response containing the results of execution
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @ClientHeaderParam(name = HttpHeaders.AUTHORIZATION, value = "{generateAuthHeader}")
    String xquery(String query);


    /**
     * Function to add the Auth header to each REST Request
     *
     * @return Header string with the auth information
     */
    default String generateAuthHeader() {
        return "Basic " + Base64.getEncoder().encodeToString("admin:".getBytes(StandardCharsets.UTF_8));
    }

}