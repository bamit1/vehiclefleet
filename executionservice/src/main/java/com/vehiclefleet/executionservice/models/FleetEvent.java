package com.vehiclefleet.executionservice.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FleetEvent {
    private String vehicleId;
    private long time;
    private double distance;
    private int avgSpeed;
    private boolean overSpeed;
    private int avgFuelLevel;

}
