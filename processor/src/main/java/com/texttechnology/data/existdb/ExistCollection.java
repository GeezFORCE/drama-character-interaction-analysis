package com.texttechnology.data.existdb;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class ExistCollection {

    @JacksonXmlProperty(isAttribute = true)
    private String name;

    @JacksonXmlProperty(isAttribute = true)
    private Date created;

    @JacksonXmlProperty(isAttribute = true, localName = "last-modified")
    private Date lastModified;

    @JacksonXmlProperty(isAttribute = true)
    private String owner;

    @JacksonXmlProperty(isAttribute = true)
    private String group;

    @JacksonXmlProperty(isAttribute = true)
    private String permissions;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "resource", namespace = "http://exist.sourceforge.net/NS/exist")
    private List<ExistResource> resources;
}
