package com.texttechnology.data.drama;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a scene within a drama, containing a unique identifier,
 * a list of distinct speaker names, and a list of speaker objects.
 */
@Builder
@Data
public class Scene {
    String sceneId;
    List<String> distinctSpeakers;
    List<Speaker> speakers;
}
