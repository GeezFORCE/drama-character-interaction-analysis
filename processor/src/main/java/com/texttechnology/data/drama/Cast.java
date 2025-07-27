package com.texttechnology.data.drama;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a cast member with unique id, name and sex
 */
@Data
@ToString
@Builder
public class Cast {
    String id;
    String name;
    String sex;
}
