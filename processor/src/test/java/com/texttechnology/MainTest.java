
package com.texttechnology;

import io.helidon.metrics.api.MetricsFactory;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HelidonTest
class MainTest {

    @Inject
    private MetricRegistry registry;

    @Inject
    private WebTarget target;


    @Test
    void testHealth() {
        Response response = target
                .path("health")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
    }

    @AfterAll
    static void clear() {
        MetricsFactory.closeAll();
    }

}
