package com.texttechnology.extraction;

import com.texttechnology.data.drama.Cast;
import com.texttechnology.data.drama.Scene;
import com.texttechnology.data.drama.Speaker;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

@Slf4j
public class ExtractionFunctions {

    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    static {
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return switch (prefix) {
                    case "tei" -> "http://www.tei-c.org/ns/1.0";
                    case "xml" -> "http://www.w3.org/XML/1998/namespace";
                    default -> XMLConstants.NULL_NS_URI;
                };
            }

            @Override
            public String getPrefix(String uri) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String uri) {
                return null;
            }
        });
    }

    /**
     * Evaluate the xpath and get the title of the drama
     */
    public static Function<Document, String> getTitle = doc -> Try.of(() -> xpath.evaluate("//tei:title", doc, XPathConstants.STRING))
            .map(String::valueOf)
            .peek(title -> log.debug("Title: {}", title))
            .getOrNull();


    /**
     * Evaluate the xpath to get the published data of the drama
     */
    public static Function<Document, String> getDate = doc -> Try.of(() -> xpath.evaluate("//tei:event/@when", doc, XPathConstants.STRING))
            .map(String::valueOf)
            .peek(date -> log.debug("Date: {}", date))
            .getOrNull();


    /**
     * Evaluate xpath to get the author details of the drama
     */
    public static Function<Document, String> getAuthor = doc -> Try.of(() -> {
                String foreName = xpath.evaluate("//tei:author/tei:forename", doc, XPathConstants.STRING).toString();
                String surname = xpath.evaluate("//tei:author/tei:surname", doc, XPathConstants.STRING).toString();
                return Optional.of(foreName.strip() + " " + surname.strip())
                        .filter(_ -> !(foreName.isBlank() && surname.isBlank()))
                        .orElseGet(xpath.evaluate("//tei:author/tei:persName", doc, XPathConstants.STRING)::toString);
            })
            .peek(author -> log.debug("Author: {}", author))
            .getOrNull();

    /**
     * Evaluate xpath to get the list of cast of the drama
     * For each cast; their ID, name and gener are extracted
     */
    public static Function<Document, List<Cast>> getCast = doc -> Try.of(() -> {
                NodeList persons = (NodeList) xpath.evaluate("//tei:listPerson/tei:person", doc, XPathConstants.NODESET);
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
     * Function to get the scenes of the play
     * Each scene object will contain the speaker details along with the lines spoken by each speaker
     *
     * @param doc DOM
     * @return list of scenes in the drama
     * @status WIP
     */
    @SneakyThrows
    public static List<Scene> getScenes(Document doc) {
        NodeList divList = (NodeList) xpath.evaluate("//tei:body/tei:div", doc, XPathConstants.NODESET);
        List<Scene> sceneList = new ArrayList<>();
        for (int i = 0; i < divList.getLength(); i++) {
            String sceneId = divList.item(i).getAttributes().getNamedItem("xml:id").getNodeValue();
            String expr = String.format("//tei:body/tei:div[@xml:id= '%s']//tei:sp", sceneId);
            NodeList spList = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
            List<Speaker> speakers = new ArrayList<Speaker>();
            for (int j = 0; j < spList.getLength(); j++) {
                if (Optional.ofNullable(spList.item(j).getAttributes().getNamedItem("who")).isPresent()) {
                    String speakerId = spList.item(j).getAttributes().getNamedItem("who").getNodeValue();
                    String lineExpr = String.format("//tei:body/tei:div[@xml:id= '%s']//tei:sp[@who = '%s']//tei:l", sceneId, speakerId);
                    NodeList lineList = (NodeList) xpath.evaluate(lineExpr, doc, XPathConstants.NODESET);

                    List<String> lineStrings = IntStream.range(0, lineList.getLength())
                            .mapToObj(lineList::item)
                            .map(node -> node.getTextContent().trim())
                            .toList();
                    speakers.add(Speaker.builder().speaker(speakerId).lines(lineStrings).build());
                }
            }
            List<String> distinctSpeakers = speakers.stream().map(Speaker::getSpeaker).distinct().toList();
            sceneList.add(Scene.builder().distinctSpeakers(distinctSpeakers).speakers(speakers).build());
        }
        return sceneList;
    }
}
