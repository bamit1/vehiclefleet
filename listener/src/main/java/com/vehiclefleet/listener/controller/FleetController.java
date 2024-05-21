package com.vehiclefleet.listener.controller;

import com.vehiclefleet.listener.models.FleetUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequestMapping("/fleet")
@RestController
public class FleetController {
    final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(FleetController.class);

    public FleetController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public void update(@RequestBody FleetUpdateEvent event) {
        CompletableFuture<SendResult<String, Object>> future = this.kafkaTemplate.send("fleet-update-events", event);
        try {
            LOGGER.info("Published event to kafka with offset: {}", future.get().getRecordMetadata().offset());
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Exception while sending event to kafka", e);
            throw new RuntimeException("Failed to send event to kafka!");
        }
    }
}
