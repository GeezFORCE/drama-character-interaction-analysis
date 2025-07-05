package com.texttechnology.visualizer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

@ApplicationScoped
@ApplicationPath("/")
@OpenAPIDefinition(
        info = @Info(
                title = "Drama Character Interaction Visualizer",
                version = "1.0.0",
                description = "Visualizes character interactions in dramas",
                contact = @Contact(
                        name = "Drama Character Interaction Analysis",
                        url = "https://github.com/yourusername/drama-character-interaction-analysis"),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"))
)
public class VisualizerApplication extends Application {
}