package com.vehiclefleet.models;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class FleetUpdateEvent {
    private String vehicleId;
    private double lat;
    private double lng;
    private int speed;
    private int fuelLevel;
    private Timestamp time;
}
