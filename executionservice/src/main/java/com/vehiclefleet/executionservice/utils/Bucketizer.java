package com.vehiclefleet.executionservice.utils;

import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Bucketizer {

    private @Value("${processor.bucketInterval}") long bucketInterval;

    public Map<Long, List<FleetUpdateEvent>> createBuckets(FleetUpdateEvent last, Set<FleetUpdateEvent> events) {
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
}
