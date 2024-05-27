package com.vehiclefleet.executionservice.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclefleet.executionservice.models.FleetEvent;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaFleetEventSerializer implements Serializer<FleetEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String s, FleetEvent fleetEvent) {
        try {
            return objectMapper.writeValueAsBytes(fleetEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
