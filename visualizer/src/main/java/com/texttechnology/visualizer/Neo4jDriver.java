package com.texttechnology.visualizer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

@ApplicationScoped
public class Neo4jDriver {

    @Inject
    @ConfigProperty(name = "neo4j.uri")
    String uri;

    @Inject
    @ConfigProperty(name = "neo4j.authentication.username")
    String username;

    @Inject
    @ConfigProperty(name = "neo4j.authentication.password")
    String password;

    @Produces
    @ApplicationScoped
    public Driver createDriver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void closeDriver(@Disposes Driver driver) {
        driver.close();
    }
}