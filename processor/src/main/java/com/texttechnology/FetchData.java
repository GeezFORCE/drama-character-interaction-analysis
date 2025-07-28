package com.texttechnology;

import com.texttechnology.extraction.DramaDataExtraction;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import restclients.ExistDbRestClient;

@Slf4j
@RequestScoped
@Path("/process")
public class FetchData {

    @Inject
    @RestClient
    ExistDbRestClient existDbRestClient;

    @Inject
    DramaRepository dramaRepository;

    /**
     * Loads the drama inserted into the eXist-db by the scraper and process it.
     * Triggered by scrapper when a play is inserted into eXist-db
     *
     * @return REST response accepted
     */
    @POST
    @Path("/{drama}")
    public Response fetch(@PathParam("drama") String drama) {
        log.info("Fetching data from Exist DB");
        dramaRepository.insertDrama(new DramaDataExtraction(existDbRestClient.getPlayXML(drama)).extractData());
        log.info("Inserted drama {} into Neo4J", drama);
        return Response.accepted().build();
    }

}
