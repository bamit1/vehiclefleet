package com.vehiclefleet.executionservice.kafka;

import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import com.vehiclefleet.executionservice.service.EventProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class FleetUpdateEventConsumer {

    @Autowired
    private EventProcessorService eventProcessorService;

    @KafkaListener(topics = "fleet-update-events", groupId = "execution-service")
    public void consumeFleetUpdateEvent(FleetUpdateEvent event) {
        eventProcessorService.processFleetUpdateEvent(event);
    }
}
