package com.texttechnology.data.drama;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class Cast {
    String id;
    String name;
    String sex;
}
