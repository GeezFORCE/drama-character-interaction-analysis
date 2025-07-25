package com.texttechnology;

import com.texttechnology.data.drama.Drama;
import com.texttechnology.data.drama.Scene;
import com.texttechnology.data.drama.Speaker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;

import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class DramaRepository {

    private final Driver driver;

    private static final String CREATE_DRAMA_NODE = """
            MERGE (d:Drama {title: $title})
            SET d.authorName = $authorName,
                d.date = $date,
                d.createdAt = datetime()
            """;

    private static final String CREATE_CAST_NODE = """
            MATCH (d:Drama {title: $dramaTitle})
            MERGE (c:Character {castId: $castId})
            SET c.name = $name,
                c.sex = $sex
            MERGE (d)-[:HAS_CHARACTER]->(c)
            """;

    private static final String CREATE_SCENE_NODE = """
            MATCH (d:Drama {title: $dramaTitle})
                MERGE (s:Scene {dramaTitle: $dramaTitle, sceneId: $sceneId})
                SET s.distinctSpeakers = $distinctSpeakers,
                    s.speakerCount = $speakerCount
                MERGE (d)-[:HAS_SCENE]->(s)
            """;

    private static final String CREATE_SPEAKER_RELATION = """
            MATCH (d:Drama {title: $dramaTitle})
            MATCH (s:Scene {dramaTitle: $dramaTitle, sceneId: $sceneId})
            MATCH (d)-[:HAS_CHARACTER]->(c:Character {castId: $castId})
            MERGE (c)-[r:SPEAKS_IN]->(s)
            SET r.lineCount = $lineCount,
                r.lines = $lines
            """;

    @Inject
    public DramaRepository(Driver driver) {
        this.driver = driver;
    }

    public void insertDrama(Drama drama) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                insertDramaWithTransaction(tx, drama);
                return null;
            });
        }
    }

    private void insertDramaWithTransaction(Transaction tx, Drama drama) {
        // 1. Create Drama node
        createDramaNode(tx, drama);

        // 2. Create Cast/Character nodes
        createCastNodes(tx, drama);

        // 3. Create Scene nodes and relationships
        createScenesAndInteractions(tx, drama);
    }

    private void createDramaNode(Transaction tx, Drama drama) {
        tx.run(CREATE_DRAMA_NODE, Values.parameters(
                "title", drama.getTitle(),
                "authorName", drama.getAuthorName(),
                "date", drama.getDate()
        ));
    }

    private void createCastNodes(Transaction tx, Drama drama) {
        drama.getCastList().stream().peek(cast -> log.error("Cast ID : {}", cast.getId())).forEach(cast -> tx.run(CREATE_CAST_NODE, Values.parameters(
                "dramaTitle", drama.getTitle(),
                "castId", cast.getId(),
                "name", cast.getName(),
                "sex", cast.getSex()
        )));
    }

    private void createScenesAndInteractions(Transaction tx, Drama drama) {
        if (drama.getScenes() == null) return;

        for (int sceneIndex = 0; sceneIndex < drama.getScenes().size(); sceneIndex++) {
            Scene scene = drama.getScenes().get(sceneIndex);
            int sceneNumber = sceneIndex + 1;

            // Create Scene node
            createSceneNode(tx, drama.getTitle(), scene);

            // Create speaker interactions within the scene
            createSpeakerInteractions(tx, drama.getTitle(), scene);

            // Create character co-appearance relationships
            createCoAppearanceRelationships(tx, drama.getTitle(), scene);
        }
    }

    private void createSceneNode(Transaction tx, String dramaTitle, Scene scene) {
        tx.run(CREATE_SCENE_NODE, Values.parameters(
                "dramaTitle", dramaTitle,
                "sceneId", scene.getSceneId(),
                "distinctSpeakers", scene.getDistinctSpeakers(),
                "speakerCount", scene.getDistinctSpeakers() != null ? scene.getDistinctSpeakers().size() : 0
        ));
    }

    private void createSpeakerInteractions(Transaction tx, String dramaTitle, Scene scene) {
        if (scene.getSpeakers() == null) return;

        scene.getSpeakers()
                .forEach(speaker -> {
                    tx.run(CREATE_SPEAKER_RELATION, Values.parameters(
                            "dramaTitle", dramaTitle,
                            "sceneId", scene.getSceneId(),
                            "castId", speaker.getSpeaker(),
                            "lineCount", speaker.getLines() != null ? speaker.getLines().size() : 0,
                            "lines", speaker.getLines()
                    ));
                });
        // Create sequential dialogue interactions
        createDialogueSequence(tx, dramaTitle, scene);
    }

    private void createDialogueSequence(Transaction tx, String dramaTitle, Scene scene) {
        if (scene.getSpeakers() == null || scene.getSpeakers().size() < 2) return;

        List<Speaker> speakers = scene.getSpeakers();
        for (int i = 0; i < speakers.size() - 1; i++) {
            Speaker currentSpeaker = speakers.get(i);
            Speaker nextSpeaker = speakers.get(i + 1);

            // Don't create self-dialogue relationships
            if (!currentSpeaker.getSpeaker().equals(nextSpeaker.getSpeaker())) {
                // Create bidirectional INTERACTS_WITH relationship to represent character interaction
                String cypherInteraction = """
                        MATCH (d:Drama {title: $dramaTitle})-[:HAS_CHARACTER]->(c1:Character)
                        MATCH (d)-[:HAS_CHARACTER]->(c2:Character)
                        WHERE c1.castId = $speaker1 AND c2.castId = $speaker2
                        MATCH (s:Scene {dramaTitle: $dramaTitle, sceneId: $sceneId})
                        MERGE (c1)-[r:INTERACTS_WITH]-(c2)
                        SET r.interactionCount = COALESCE(r.interactionCount, 0) + 1,
                            r.lastScene = $sceneId,
                            r.dramaTitle = $dramaTitle
                        MERGE (c1)-[:INTERACTS_IN]->(s)
                        MERGE (c2)-[:INTERACTS_IN]->(s)
                        """;

                tx.run(cypherInteraction, Values.parameters(
                        "speaker1", currentSpeaker.getSpeaker(),
                        "speaker2", nextSpeaker.getSpeaker(),
                        "dramaTitle", dramaTitle,
                        "sceneId", scene.getSceneId()
                ));

                // Also maintain the original directional DIALOGUES_WITH relationship
                String cypherDialogue = """
                        MATCH (d:Drama {title: $dramaTitle})-[:HAS_CHARACTER]->(c1:Character)
                        MATCH (d)-[:HAS_CHARACTER]->(c2:Character)
                        WHERE c1.castId = $speaker1 AND c2.castId = $speaker2
                        MATCH (s:Scene {dramaTitle: $dramaTitle, sceneId: $sceneId})
                        MERGE (c1)-[r:DIALOGUES_WITH]->(c2)
                        SET r.sceneCount = COALESCE(r.sceneCount, 0) + 1
                        """;

                tx.run(cypherDialogue, Values.parameters(
                        "speaker1", currentSpeaker.getSpeaker(),
                        "speaker2", nextSpeaker.getSpeaker(),
                        "dramaTitle", dramaTitle,
                        "sceneId", scene.getSceneId()
                ));
            }
        }
    }

    private void createCoAppearanceRelationships(Transaction tx, String dramaTitle, Scene scene) {
        if (scene.getDistinctSpeakers() == null || scene.getDistinctSpeakers().size() < 2) return;

        List<String> speakers = scene.getDistinctSpeakers();

        // Create co-appearance relationships for all pairs of characters in the scene
        for (int i = 0; i < speakers.size(); i++) {
            for (int j = i + 1; j < speakers.size(); j++) {
                String speaker1 = speakers.get(i);
                String speaker2 = speakers.get(j);

                String cypher = """
                        MATCH (d:Drama {title: $dramaTitle})-[:HAS_CHARACTER]->(c1:Character)
                        MATCH (d)-[:HAS_CHARACTER]->(c2:Character)
                        WHERE c1.castId = $speaker1 AND c2.castId = $speaker2
                        MATCH (s:Scene {dramaTitle: $dramaTitle, sceneId: $sceneId})
                        MERGE (c1)-[r:APPEARS_WITH]-(c2)
                        SET r.coAppearanceCount = COALESCE(r.coAppearanceCount, 0) + 1
                        """;

                tx.run(cypher, Values.parameters(
                        "speaker1", speaker1,
                        "speaker2", speaker2,
                        "dramaTitle", dramaTitle,
                        "sceneId", scene.getSceneId()
                ));
            }
        }
    }

    public List<Map<String, Object>> getCharacterInteractions(String dramaTitle) {
        try (Session session = driver.session()) {
            String cypher = """
                    MATCH (d:Drama {title: $dramaTitle})-[:HAS_CHARACTER]->(c1:Character)
                    MATCH (c1)-[r]->(c2:Character)
                    WHERE type(r) IN ['DIALOGUES_WITH', 'INTERACTS_WITH']
                    RETURN c1.name as character1, c2.name as character2, 
                           CASE type(r) 
                             WHEN 'DIALOGUES_WITH' THEN r.sceneCount 
                             WHEN 'INTERACTS_WITH' THEN r.interactionCount 
                             ELSE 0 
                           END as interactionCount,
                           type(r) as relationshipType
                    ORDER BY interactionCount DESC
                    """;

            return session.run(cypher, Values.parameters("dramaTitle", dramaTitle))
                    .list(record -> Map.of(
                            "character1", record.get("character1").asString(),
                            "character2", record.get("character2").asString(),
                            "interactionCount", record.get("interactionCount").asInt(),
                            "relationshipType", record.get("relationshipType").asString()
                    ));
        }
    }

    // Method to get scene analysis
    public List<Map<String, Object>> getSceneAnalysis(String dramaTitle) {
        try (Session session = driver.session()) {
            String cypher = """
                    MATCH (d:Drama {title: $dramaTitle})-[:HAS_SCENE]->(s:Scene)
                    OPTIONAL MATCH (c:Character)-[:SPEAKS_IN]->(s)
                    RETURN s.number as sceneNumber, 
                           s.speakerCount as speakerCount,
                           collect(c.name) as characters,
                           s.distinctSpeakers as distinctSpeakers
                    ORDER BY s.number
                    """;

            return session.run(cypher, Values.parameters("dramaTitle", dramaTitle))
                    .list(record -> Map.of(
                            "sceneNumber", record.get("sceneNumber").asInt(),
                            "speakerCount", record.get("speakerCount").asInt(),
                            "characters", record.get("characters").asList(),
                            "distinctSpeakers", record.get("distinctSpeakers").asList()
                    ));
        }
    }

    // Method to get detailed character interactions
    public List<Map<String, Object>> getDetailedCharacterInteractions(String dramaTitle) {
        try (Session session = driver.session()) {
            String cypher = """
                    MATCH (d:Drama {title: $dramaTitle})-[:HAS_CHARACTER]->(c1:Character)
                    MATCH (c1)-[r:INTERACTS_WITH]-(c2:Character)
                    WHERE c1.name < c2.name  // To avoid duplicate pairs
                    RETURN c1.name as character1, 
                           c2.name as character2, 
                           r.interactionCount as interactionCount,
                           r.lastScene as lastScene,
                           r.dramaTitle as dramaTitle,
                           [(c1)-[:INTERACTS_IN]->(s:Scene)<-[:INTERACTS_IN]-(c2) | s.number] as sharedScenes
                    ORDER BY r.interactionCount DESC
                    """;

            return session.run(cypher, Values.parameters("dramaTitle", dramaTitle))
                    .list(record -> Map.of(
                            "character1", record.get("character1").asString(),
                            "character2", record.get("character2").asString(),
                            "interactionCount", record.get("interactionCount").asInt(),
                            "lastScene", record.get("lastScene").asInt(),
                            "dramaTitle", record.get("dramaTitle").asString(),
                            "sharedScenes", record.get("sharedScenes").asList()
                    ));
        }
    }
}
