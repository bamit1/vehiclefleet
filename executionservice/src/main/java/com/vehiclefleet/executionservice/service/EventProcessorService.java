package com.vehiclefleet.executionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclefleet.executionservice.models.FleetEvent;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventProcessorService {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    private @Value("${processor.triggerInterval}") long triggerInterval;
    private @Value("${processor.bucketInterval}") long bucketInterval;
    private @Value("${processor.overSpeedThreshold}") int overSpeedThreshold;

    private static final String SORTED_SET_KEY_PREFIX = "sortedSet:";
    private static final String LAST_PROCESSED_KEY_PREFIX = "lastProcessed:";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, FleetUpdateEvent> redisTemplate;

    public void processFleetUpdateEvent(String event) throws JsonProcessingException {
        FleetUpdateEvent fleetUpdateEvent = objectMapper.readValue(event, FleetUpdateEvent.class);

        process(fleetUpdateEvent);

        redisTemplate.opsForZSet().add(SORTED_SET_KEY_PREFIX + fleetUpdateEvent.getVehicleId(), fleetUpdateEvent, fleetUpdateEvent.getTime());
    }

    private void process(FleetUpdateEvent event) {
        FleetUpdateEvent lastUpdated = redisTemplate.opsForValue().get(LAST_PROCESSED_KEY_PREFIX + event.getVehicleId());
        if (lastUpdated == null) {
            redisTemplate.opsForValue().set(LAST_PROCESSED_KEY_PREFIX + event.getVehicleId(), event);
            return;
        }
        if (event.getTime() - lastUpdated.getTime() < triggerInterval) return;

        Set<FleetUpdateEvent> events = redisTemplate.opsForZSet().range(SORTED_SET_KEY_PREFIX + event.getVehicleId(), 0, -1);

        Map<Long, List<FleetUpdateEvent>> buckets = createBuckets(lastUpdated, events);
        List<FleetEvent> fleetEvents = buckets.values().stream().map(this::aggregate).toList();

        fleetEvents.forEach(fleetEvent -> kafkaTemplate.send("fleet-events", fleetEvent));

        redisTemplate.opsForZSet().remove(SORTED_SET_KEY_PREFIX + event.getVehicleId(), events.toArray());
        redisTemplate.opsForValue().set(LAST_PROCESSED_KEY_PREFIX + event.getVehicleId(), event);
    }


    private Map<Long, List<FleetUpdateEvent>> createBuckets(FleetUpdateEvent last, Set<FleetUpdateEvent> events) {
        List<FleetUpdateEvent> eventsList = new ArrayList<>(events);
        Map<Long, List<FleetUpdateEvent>> buckets = new HashMap<>();
        for (int i = 0; i < events.size(); i++) {
            long time = eventsList.get(i).getTime();
            long floor = (time / (bucketInterval)) * bucketInterval;
            if (!buckets.containsKey(floor)) {
                buckets.put(floor, new ArrayList<>());
                buckets.get(floor).add(last);
            }
            buckets.get(floor).add(eventsList.get(i));
            last = eventsList.get(i);
        }
        return buckets;
    }

    private FleetEvent aggregate(List<FleetUpdateEvent> events) {
        double distance = 0.0;
        int speed = 0;
        int fuelLevel = 0;
        boolean overSpeed = true;
        for (int i = 1; i < events.size(); i++) {
            distance += calculateEuclideanDistance(events.get(i - 1), events.get(i));
            speed += events.get(i).getSpeed();
            fuelLevel += events.get(i).getFuelLevel();
            overSpeed = events.get(i).getSpeed() >= overSpeedThreshold && overSpeed;
        }
        return FleetEvent.builder()
                .vehicleId(events.get(0).getVehicleId())
                .distance(distance)
                .avgSpeed(speed / (events.size() - 1))
                .avgFuelLevel(fuelLevel / (events.size() - 1))
                .overSpeed(overSpeed)
                .time(events.get(1).getTime())
                .build();
    }

    private double calculateEuclideanDistance(FleetUpdateEvent x, FleetUpdateEvent y) {
        return Math.sqrt(Math.pow(y.getLng() - x.getLng(), 2) + Math.pow(y.getLat() - x.getLat(), 2));
    }
}


