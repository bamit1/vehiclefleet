package com.vehiclefleet.executionservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vehiclefleet.executionservice.service.EventProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class Consumer {

    @Autowired
    private EventProcessorService eventProcessorService;

    @KafkaListener(topics = "fleet-update-events", groupId = "execution-service")
    public void listenGroupFoo(String event) throws JsonProcessingException {
        eventProcessorService.processFleetUpdateEvent(event);
    }
}
