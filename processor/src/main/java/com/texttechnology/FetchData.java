package com.texttechnology;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.texttechnology.data.drama.Drama;
import com.texttechnology.data.existdb.ExistResource;
import com.texttechnology.data.existdb.ExistResult;
import com.texttechnology.extraction.DramaDataExtraction;
import io.helidon.microprofile.scheduling.FixedRate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import restclients.ExistDbRestClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class FetchData {

    @Inject
    @RestClient
    ExistDbRestClient existDbRestClient;

    XmlMapper mapper = new XmlMapper();

    @SneakyThrows
    @FixedRate(initialDelay = 5, value = 60, timeUnit = TimeUnit.SECONDS)
    public void fetch() {
        log.info("Fetching data from Exist DB");

        String allInsertedPlays = existDbRestClient.getAllInsertedPlays();

        List<String> insertedPlays = mapper
                .readValue(allInsertedPlays, ExistResult.class)
                .getCollection()
                .getResources()
                .stream()
                .filter(ExistResource::isXmlFile)
                .map(ExistResource::getName)
                .map(name -> name.substring(0, name.lastIndexOf('.')))
                .toList();

        log.info("Inserted plays: {}", insertedPlays);


        List<Drama> dramas = insertedPlays
                .stream()
                .map(play -> existDbRestClient.getPlayXML(play))
                .map(play -> new DramaDataExtraction(play).extractData())
                .toList();

        dramas.forEach(drama -> log.info("Drama: {}", drama.toString()));

    }

}
