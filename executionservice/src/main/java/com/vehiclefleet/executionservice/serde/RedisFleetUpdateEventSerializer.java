package com.vehiclefleet.executionservice.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

public class RedisFleetUpdateEventSerializer implements RedisSerializer<FleetUpdateEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(FleetUpdateEvent value) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FleetUpdateEvent deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) return null;
        try {
            return objectMapper.readValue(bytes, FleetUpdateEvent.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
