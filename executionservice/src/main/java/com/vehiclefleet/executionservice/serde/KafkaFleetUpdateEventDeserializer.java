package com.vehiclefleet.executionservice.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class KafkaFleetUpdateEventDeserializer implements Deserializer<FleetUpdateEvent> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FleetUpdateEvent deserialize(String s, byte[] bytes) {
        if (bytes == null) return null;
        try {
            return objectMapper.readValue(bytes, FleetUpdateEvent.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
