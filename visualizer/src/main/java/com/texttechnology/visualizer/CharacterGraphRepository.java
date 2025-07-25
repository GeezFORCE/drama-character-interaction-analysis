package com.texttechnology.visualizer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class CharacterGraphRepository {

    private final Driver driver;

    @Inject
    public CharacterGraphRepository(Driver driver) {
        this.driver = driver;
    }

    /**
     * Get character interaction data for visualization
     * @param dramaTitle The title of the drama
     * @return A map containing nodes and links for the graph visualization
     */
    public Map<String, Object> getCharacterInteractionGraph(String dramaTitle) {
        try (Session session = driver.session()) {
            // Get characters (nodes)
            String characterQuery = """
                    MATCH (d:Drama {title: $dramaTitle})-[:HAS_CHARACTER]->(c:Character)
                    RETURN c.name as name, c.sex as gender
                    """;
            
            List<Map<String, Object>> nodes = session.run(characterQuery, Values.parameters("dramaTitle", dramaTitle))
                    .list(record -> Map.of(
                            "id", record.get("name").asString(),
                            "name", record.get("name").asString(),
                            "gender", record.get("gender") != null ? record.get("gender").asString() : "unknown"
                    ));
            
            // Get interactions (links)
            String interactionQuery = """
                    MATCH (d:Drama {title: $dramaTitle})-[:HAS_CHARACTER]->(c1:Character)
                    MATCH (c1)-[r:INTERACTS_WITH]-(c2:Character)
                    WHERE c1.name < c2.name  // To avoid duplicate pairs
                    RETURN c1.name as source, c2.name as target, r.interactionCount as value
                    """;
            
            List<Map<String, Object>> links = session.run(interactionQuery, Values.parameters("dramaTitle", dramaTitle))
                    .list(record -> Map.of(
                            "source", record.get("source").asString(),
                            "target", record.get("target").asString(),
                            "value", record.get("value").asInt()
                    ));
            
            Map<String, Object> result = new HashMap<>();
            result.put("nodes", nodes);
            result.put("links", links);
            
            return result;
        }
    }
    
    /**
     * Get a list of all drama titles in the database
     * @return List of drama titles
     */
    public List<String> getAllDramaTitles() {
        try (Session session = driver.session()) {
            String query = "MATCH (d:Drama) RETURN d.title as title ORDER BY title";
            return session.run(query).list(record -> record.get("title").asString());
        }
    }
}