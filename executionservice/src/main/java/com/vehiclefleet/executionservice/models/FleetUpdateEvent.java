package com.vehiclefleet.executionservice.models;

import lombok.Data;

@Data
public class FleetUpdateEvent {
    private String vehicleId;
    private double lat;
    private double lng;
    private int speed;
    private int fuelLevel;
    private long time;

    public long getTime() {
        return (time / (1000 * 60)) * 1000 * 60;
    }
}
