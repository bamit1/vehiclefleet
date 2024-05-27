package com.vehiclefleet.executionservice.service;

import com.vehiclefleet.executionservice.aggregators.EuclideanAggregator;
import com.vehiclefleet.executionservice.models.FleetEvent;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import com.vehiclefleet.executionservice.redis.CacheClient;
import com.vehiclefleet.executionservice.utils.Bucketizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.vehiclefleet.executionservice.utils.Constants.LAST_PROCESSED_KEY_PREFIX;
import static com.vehiclefleet.executionservice.utils.Constants.SORTED_SET_KEY_PREFIX;

@Slf4j
@Component
public class EventProcessorService {

    private @Value("${processor.triggerInterval}") long triggerInterval;

    @Autowired
    private KafkaTemplate<String, FleetEvent> kafkaTemplate;

    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private EuclideanAggregator aggregator;
    @Autowired
    private Bucketizer bucketizer;

    /**
     * for every latest update event, checks the last processed from cache and
     * processes the pending events based on {@code triggerInterval} and
     * inserts the latest event in the cache
     * */
    public void processFleetUpdateEvent(FleetUpdateEvent fleetUpdateEvent) {
        FleetUpdateEvent lastProcessed = getLastProcessedEvent(fleetUpdateEvent);
        if (lastProcessed != null && fleetUpdateEvent.getTime() - lastProcessed.getTime() >= triggerInterval) {
            processPendingEvents(fleetUpdateEvent, lastProcessed);
        }
        cacheClient.addToSet(SORTED_SET_KEY_PREFIX + fleetUpdateEvent.getVehicleId(), fleetUpdateEvent);
    }

    /**
     * Fetches the processing pending events from cache, then bucketizes and aggregates them,
     * then publishes the aggregated data to kafka and updates the cache.
     * */
    private void processPendingEvents(FleetUpdateEvent fleetUpdateEvent, FleetUpdateEvent lastProcessed) {
        // fetch pending events from cache
        Set<FleetUpdateEvent> events = cacheClient.getFromSet(SORTED_SET_KEY_PREFIX + fleetUpdateEvent.getVehicleId(), 0, -1);

        // bucketize the events based on time
        Map<Long, List<FleetUpdateEvent>> buckets = bucketizer.createBuckets(lastProcessed, events);

        // aggregate each bucket
        List<FleetEvent> fleetEvents = buckets.values().stream().map(aggregator::aggregate).toList();

        // publish the aggregated data
        fleetEvents.forEach(fleetEvent -> kafkaTemplate.send("fleet-events", fleetEvent));

        // remove the processed events from cache
        cacheClient.removeFromSet(SORTED_SET_KEY_PREFIX + fleetUpdateEvent.getVehicleId(), events.toArray(FleetUpdateEvent[]::new));

        // update last processed
        cacheClient.setLastProcessed(LAST_PROCESSED_KEY_PREFIX + fleetUpdateEvent.getVehicleId(), fleetUpdateEvent);
    }

    /**
     * Fetch last processed from cache for a given vehicle
     * if last processed is not in cache, update the cache with the current event
     * */
    private FleetUpdateEvent getLastProcessedEvent(FleetUpdateEvent fleetUpdateEvent) {
        FleetUpdateEvent lastProcessed = cacheClient.getLastProcessed(LAST_PROCESSED_KEY_PREFIX + fleetUpdateEvent.getVehicleId());
        if (lastProcessed == null) {
            cacheClient.setLastProcessed(LAST_PROCESSED_KEY_PREFIX + fleetUpdateEvent.getVehicleId(), fleetUpdateEvent);
        }
        return lastProcessed;
    }
}


