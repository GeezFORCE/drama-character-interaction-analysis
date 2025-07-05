package com.texttechnology;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import restclients.DracorRestClient;
import restclients.ExistDbRestClient;
import restclients.ProcessorRestClient;

@Path("/load")
@RequestScoped
@Slf4j
public class LoadDataSet {
    @Inject
    @RestClient
    DracorRestClient dracorRestClient;

    @Inject
    @RestClient
    ExistDbRestClient existDbRestClient;

    @Inject
    @RestClient
    ProcessorRestClient processorRestClient;

    @Inject
    @ConfigProperty(name = "processor/mp-rest/url")
    String endpointUrl;

    @PostConstruct
    void logEndpointUrl() {
        log.info("Processor endpoint URL: {}", endpointUrl);
    }


    /**
     * API Endpoint to load specific dramas (mainly for testing)
     *
     * @param body JSON Array listing the dramas to be loaded (must be the ID of the drama in DraCor)
     * @return REST Response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.OBJECT)))
    public Response loadDataSet(JsonArray body) {
        log.info("Received body: {}", body);

        // Extracts the plays in the body one by one and creates a list
        var plays = body.stream()
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .map(String::trim)
                .toList();

        // Insert the plays to the eXist DB
        plays.forEach(this::insertPlayToExistDb);

        return Response.ok().build();
    }


    /**
     * API Endpoint to handle the request to load all the data from Dracor
     *
     * @return REST Response
     */
    @PUT
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadEntireDataSet() {
        log.info("Getting details of all plays");
        JsonArrayBuilder playDetails = Json.createArrayBuilder();

        // Requests Dracor for all the plays and for each retrieve the XML content and inserts it into the eXist DB
        dracorRestClient.getAllPlayDetails()
                .getJsonArray("plays")
                .stream()
                .map(JsonValue::asJsonObject)
                .map(jsonObject -> jsonObject.getString("name"))
                .limit(30)
                .peek(play -> log.info("Loading play: {}", play))
                .forEach(play -> {
                    playDetails.add(play);
                    insertPlayToExistDb(play);
                    processorRestClient.process(play);
                });
        return Response.ok()
                .entity(playDetails.build())
                .build();
    }


    /**
     * Gets the TEI content of the drama
     *
     * @param play Name of the play
     */
    private void insertPlayToExistDb(String play) {
        try (Response r = existDbRestClient.insertPlay(play, dracorRestClient.getTeiForPlay(play))) {
            log.info("Response Status: {}", r.getStatus());
            log.info("Response Body : {}", r.readEntity(String.class));
        } catch (Exception e) {
            log.error("Error inserting play {} : {}", play, e.toString());
        } finally {
            log.info("Finished loading play: {}", play);
        }
    }

}
