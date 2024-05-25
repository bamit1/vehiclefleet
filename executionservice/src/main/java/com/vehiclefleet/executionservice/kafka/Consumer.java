package com.vehiclefleet.executionservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class Consumer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "fleet-update-events", groupId = "execution-service")
    public void listenGroupFoo(String event) throws JsonProcessingException {
        FleetUpdateEvent fleetUpdateEvent = objectMapper.readValue(event, FleetUpdateEvent.class);
        redisTemplate.opsForZSet().add(fleetUpdateEvent.getVehicleId(), fleetUpdateEvent.toString(), fleetUpdateEvent.getTime());
        log.info("Received Message: " + fleetUpdateEvent);
    }
}
