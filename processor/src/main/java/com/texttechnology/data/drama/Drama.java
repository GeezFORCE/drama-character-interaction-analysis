package com.texttechnology.data.drama;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Drama {
    private String title;
    private String authorName;
    String date;
    List<Cast> castList;
    List<Scene> scenes;
}
