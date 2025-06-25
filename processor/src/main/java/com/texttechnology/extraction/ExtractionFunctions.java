package com.texttechnology.extraction;

import com.texttechnology.data.drama.Cast;
import com.texttechnology.data.drama.Scene;
import com.texttechnology.data.drama.Speaker;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

@Slf4j
public class ExtractionFunctions {

    private static final XPath xpath = XPathFactory.newInstance().newXPath();


    /**
     * Evaluate the xpath and get the title of the drama
     */
    public static Function<Document, String> getTitle = doc -> Try.of(() -> xpath.evaluate("//title", doc, XPathConstants.STRING))
            .map(String::valueOf)
            .peek(title -> log.debug("Title: {}", title))
            .getOrNull();


    /**
     * Evaluate the xpath to get the published data of the drama
     */
    public static Function<Document, String> getDate = doc -> Try.of(() -> xpath.evaluate("//event/@when", doc, XPathConstants.STRING))
            .map(String::valueOf)
            .peek(date -> log.debug("Date: {}", date))
            .getOrNull();


    /**
     * Evaluate xpath to get the author details of the drama
     */
    public static Function<Document, String> getAuthor = doc -> Try.of(() -> {
                String foreName = xpath.evaluate("//author/forename", doc, XPathConstants.STRING).toString();
                String surname = xpath.evaluate("//author/surname", doc, XPathConstants.STRING).toString();
                return Optional.of(foreName.strip() + " " + surname.strip())
                        .filter(_ -> !(foreName.isBlank() && surname.isBlank()))
                        .orElseGet(xpath.evaluate("//author/persName", doc, XPathConstants.STRING)::toString);
            })
            .peek(author -> log.debug("Author: {}", author))
            .getOrNull();

    /**
     * Evaluate xpath to get the list of cast of the drama
     * For each cast; their ID, name and gener are extracted
     */
    public static Function<Document, List<Cast>> getCast = doc -> Try.of(() -> {
                NodeList persons = (NodeList) xpath.evaluate("//listPerson/person", doc, XPathConstants.NODESET);
                return IntStream.range(0, persons.getLength())
                        .mapToObj(persons::item)
                        .map(Element.class::cast)
                        .map(cast -> Cast.builder()
                                .id(cast.getAttribute("id"))
                                .name(cast.getElementsByTagName("persName").item(0).getTextContent())
                                .sex(cast.getAttribute("sex"))
                                .build())
                        .toList();
            })
            .peek(castList -> log.debug("Cast List: {}", castList))
            .getOrNull();

    /**
     * Function to get the lines of the speakers in each scene
     *
     * @param scene DOM element of the scene
     * @return A Scene POJO containing the details
     * @status WIP
     */
    public static Scene getSpeakersInScene(Element scene) {
        NodeList spNodes = scene.getElementsByTagName("sp");
        var allSpeakers = IntStream.range(0, spNodes.getLength())
                .mapToObj(spNodes::item)
                .map(Element.class::cast)
                .map(spEl -> {
                    String who = spEl.getAttribute("who");
                    NodeList lNodes = spEl.getElementsByTagName("l");

                    List<String> lines = IntStream.range(0, lNodes.getLength())
                            .mapToObj(lNodes::item)
                            .map(Node::getTextContent)
                            .map(String::trim)
                            .toList();

                    return Speaker.builder()
                            .speaker(who)
                            .lines(lines)
                            .build();
                })
                .toList();

        var distinctSpeakers = allSpeakers.stream().map(Speaker::getSpeaker).distinct().toList();

        return Scene.builder().distinctSpeakers(distinctSpeakers).speakers(allSpeakers).build();

    }

    /**
     * Function to get the scenes of the play
     * Each scene object will contain the speaker details along with the lines spoken by each speaker
     *
     * @param doc DOM
     * @return list of scenes in the drama
     * @status WIP
     */
    @SneakyThrows
    public static List<Scene> getScenes(Document doc) {
        int sceneCount = 0;
        NodeList speakers = (NodeList) xpath.evaluate("//div[@type = 'scene']", doc, XPathConstants.NODESET);

        return IntStream.range(0, speakers.getLength())
                .mapToObj(speakers::item)
                .map(Element.class::cast)
                .map(ExtractionFunctions::getSpeakersInScene)
                .toList();
    }
}
