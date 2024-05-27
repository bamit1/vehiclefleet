package com.vehiclefleet.executionservice.aggregators;

import com.vehiclefleet.executionservice.models.FleetEvent;
import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EuclideanAggregator implements Aggregator {

    private @Value("${processor.overSpeedThreshold}") int overSpeedThreshold;

    @Override
    public FleetEvent aggregate(List<FleetUpdateEvent> events) {
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
