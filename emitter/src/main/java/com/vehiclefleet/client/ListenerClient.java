package com.vehiclefleet.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclefleet.models.FleetUpdateEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ListenerClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void emit(FleetUpdateEvent event) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://listener:8081/fleet"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response status code: " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
