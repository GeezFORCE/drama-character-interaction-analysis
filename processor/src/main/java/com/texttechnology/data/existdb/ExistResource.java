package com.texttechnology.data.existdb;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * Represents a resource in an eXist-db system, typically obtained via XML-based API responses.
 *
 * Fields are annotated for XML attribute binding using Jackson's XML module.
 */
@Data
@ToString
@NoArgsConstructor
public class ExistResource {
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

    public boolean isXmlFile() {
        return name != null && name.toLowerCase().endsWith(".xml");
    }
}
