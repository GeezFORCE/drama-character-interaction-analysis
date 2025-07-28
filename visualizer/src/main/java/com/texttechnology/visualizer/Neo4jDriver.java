package com.texttechnology.visualizer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/**
 * Bean responsible for instantiating a Neo4J driver
 */
@ApplicationScoped
public class Neo4jDriver {

    /**
     * URI to connect to Neo4J server
     */
    @Inject
    @ConfigProperty(name = "neo4j.uri")
    String uri;

    /**
     * Username for authentication
     */
    @Inject
    @ConfigProperty(name = "neo4j.authentication.username")
    String username;

    /**
     * Password for authentication
     */
    @Inject
    @ConfigProperty(name = "neo4j.authentication.password")
    String password;

    /**
     * Produces a Neo4J driver instance
     * @return a new driver instance connected to the server
     */
    @Produces
    @ApplicationScoped
    public Driver createDriver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    /**
     * Closes the driver instance
     * @param driver to be closed
     */
    public void closeDriver(@Disposes Driver driver) {
        driver.close();
    }


}