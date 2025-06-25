package com.texttechnology.data.existdb;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JacksonXmlRootElement(localName = "result", namespace = "http://exist.sourceforge.net/NS/exist")
@Data
@ToString
@NoArgsConstructor
public class ExistResult {

    @JacksonXmlProperty(localName = "collection", namespace = "http://exist.sourceforge.net/NS/exist")
    private ExistCollection collection;
}
