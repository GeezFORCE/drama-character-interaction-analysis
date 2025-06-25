package com.texttechnology.data.drama;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class Speaker {
    String speaker;
    List<String> lines;
}
