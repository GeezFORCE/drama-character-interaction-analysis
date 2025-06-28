package com.texttechnology.extraction;

import com.texttechnology.data.drama.Drama;
import com.texttechnology.data.drama.Drama.DramaBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static com.texttechnology.extraction.ExtractionFunctions.*;

@Slf4j
public class DramaDataExtraction {

    private final DramaBuilder drama = Drama.builder();
    private final Document playDoc;

    @SneakyThrows
    public DramaDataExtraction(String playXML) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        playDoc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(playXML)));
    }

    /**
     * @return A POJO containing the extracted data for the drama
     */
    public Drama extractData() {
        getScenes(playDoc);
        return drama.title(getTitle.apply(playDoc))
                .authorName(getAuthor.apply(playDoc))
                .date(getDate.apply(playDoc))
                .castList(getCast.apply(playDoc))
                .scenes(getScenes(playDoc))
                .build();
    }

}
