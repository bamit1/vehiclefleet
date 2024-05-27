package com.vehiclefleet.executionservice.aggregators;

import com.vehiclefleet.executionservice.models.FleetEvent;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;

import java.util.List;

public interface Aggregator {
    FleetEvent aggregate(List<FleetUpdateEvent> events);
}
