package com.texttechnology.data.drama;

import lombok.Builder;

import java.util.List;

@Builder
public class Scene {
    List<String> distinctSpeakers;
    List<Speaker> speakers;
}
