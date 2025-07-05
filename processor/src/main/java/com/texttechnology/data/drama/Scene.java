package com.texttechnology.data.drama;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Scene {
    String sceneId;
    List<String> distinctSpeakers;
    List<Speaker> speakers;
}
