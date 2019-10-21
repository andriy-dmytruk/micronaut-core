package io.micronaut.docs.server.response;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StatusControllerSpec {

    private static EmbeddedServer server;
    private static HttpClient client;

    @BeforeClass
    public static void setupServer() {
        server = ApplicationContext.run(EmbeddedServer.class, Collections.singletonMap("spec.name", "httpstatus"));
        client = server
                .getApplicationContext()
                .createBean(HttpClient.class, server.getURL());
    }

    @AfterClass
    public static void stopServer() {
        if(server != null) {
            server.stop();
        }
        if(client != null) {
            client.stop();
        }
    }

    @Test
    public void testStatus() {
        HttpResponse<String> response = client.toBlocking().exchange(HttpRequest.GET("/status"), String.class);
        Optional<String> body = response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("success", body.get());

        response = client.toBlocking().exchange(HttpRequest.GET("/status/http-response"), String.class);
        body = response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("success", body.get());

        response = client.toBlocking().exchange(HttpRequest.GET("/status/http-status"), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
    }

}
