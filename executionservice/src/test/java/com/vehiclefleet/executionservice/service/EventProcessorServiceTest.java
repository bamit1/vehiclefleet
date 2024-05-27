package com.vehiclefleet.executionservice.service;

import com.vehiclefleet.executionservice.aggregators.EuclideanAggregator;
import com.vehiclefleet.executionservice.models.FleetEvent;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import com.vehiclefleet.executionservice.redis.CacheClient;
import com.vehiclefleet.executionservice.utils.Bucketizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class EventProcessorServiceTest {

    @Mock private KafkaTemplate<String, FleetEvent> kafkaTemplate;
    @Mock private CacheClient cacheClient;
    @Mock private EuclideanAggregator aggregator;
    @Mock private Bucketizer bucketizer;
    @InjectMocks private EventProcessorService eventProcessorService;

    @Test
    public void processFleetUpdateEvent_firstEvent_insertsIntoCache() {
        FleetUpdateEvent event = createFleetUpdateEvent();
        when(cacheClient.getLastProcessed(anyString())).thenReturn(null);

        eventProcessorService.processFleetUpdateEvent(event);

        verify(cacheClient).getLastProcessed(anyString());
        verify(cacheClient).setLastProcessed(anyString(), any());
        verify(cacheClient).addToSet(anyString(), any());
        verify(cacheClient, times(0)).getFromSet(anyString(), anyInt(), anyInt());
    }

    @Test
    public void processFleetUpdateEvent_secondEvent_processesAndInsertsIntoCache() {
        FleetUpdateEvent lastProcessed = createFleetUpdateEvent();
        FleetUpdateEvent event = createFleetUpdateEvent();
        Set<FleetUpdateEvent> fleetUpdateEvents = createPendingEvents();
        Map<Long, List<FleetUpdateEvent>> buckets = createBuckets();

        when(cacheClient.getLastProcessed(anyString())).thenReturn(lastProcessed);
        when(cacheClient.getFromSet(anyString(), anyInt(), anyInt())).thenReturn(fleetUpdateEvents);
        when(bucketizer.createBuckets(lastProcessed, fleetUpdateEvents)).thenReturn(buckets);
        when(aggregator.aggregate(any())).thenReturn(createFleetEvent());

        eventProcessorService.processFleetUpdateEvent(event);

        verify(cacheClient).getLastProcessed(anyString());
        verify(cacheClient).getFromSet(anyString(), anyInt(), anyInt());
        verify(cacheClient).addToSet(anyString(), any());
        verify(bucketizer).createBuckets(any(), any());
        verify(aggregator, times(2)).aggregate(any());
        verify(kafkaTemplate,  times(2)).send(anyString(), any());
        verify(cacheClient).removeFromSet(anyString(), any());
        verify(cacheClient).setLastProcessed(anyString(), any());

    }

    private FleetEvent createFleetEvent() {
        return FleetEvent.builder()
                .vehicleId("v1")
                .distance(300.0)
                .avgFuelLevel(30)
                .overSpeed(true)
                .avgSpeed(20)
                .time(System.currentTimeMillis())
                .build();
    }

    private Map<Long, List<FleetUpdateEvent>> createBuckets() {
        Map<Long, List<FleetUpdateEvent>> result = new HashMap<>();
        result.put(1L, List.of(createFleetUpdateEvent(), createFleetUpdateEvent()));
        result.put(2L, List.of(createFleetUpdateEvent(), createFleetUpdateEvent()));
        return result;
    }

    private Set<FleetUpdateEvent> createPendingEvents() {
        return Sets.newSet(createFleetUpdateEvent(), createFleetUpdateEvent());
    }

    private FleetUpdateEvent createFleetUpdateEvent() {
        FleetUpdateEvent fleetUpdateEvent = new FleetUpdateEvent();
        fleetUpdateEvent.setVehicleId("v1");
        fleetUpdateEvent.setLat(1.0);
        fleetUpdateEvent.setLng(1.0);
        fleetUpdateEvent.setSpeed(10);
        fleetUpdateEvent.setFuelLevel(20);
        fleetUpdateEvent.setTime(System.currentTimeMillis());
        return fleetUpdateEvent;
    }
}