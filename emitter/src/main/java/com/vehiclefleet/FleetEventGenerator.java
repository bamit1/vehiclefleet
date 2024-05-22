package com.vehiclefleet;

import com.vehiclefleet.client.ListenerClient;
import com.vehiclefleet.models.FleetUpdateEvent;

import java.sql.Timestamp;
import java.util.Random;

public class FleetEventGenerator implements Runnable {

    private final String vehicleId;
    private final ListenerClient listenerClient;

    private int speed, fuelLevel;
    private double lat, lng;
    private Random random;

    public FleetEventGenerator(String vehicleId, ListenerClient listenerClient) {
        this.vehicleId = vehicleId;
        this.listenerClient = listenerClient;
        random = new Random();
        this.speed = random.nextInt(50);
        this.fuelLevel = random.nextInt(50);
        this.lat = random.nextInt(-40, 40);
        this.lng = random.nextInt(-90, 90);
    }

    @Override
    public void run() {
        System.out.println("started thread for vehicle: " + vehicleId);
        while (true) {
            lat += random.nextDouble(-0.2, 0.2);
            lng += random.nextDouble(-0.2, 0.2);
            speed += random.nextInt(-2, 2);
            fuelLevel += random.nextInt(-2, 2);

            FleetUpdateEvent event = new FleetUpdateEvent();
            event.setVehicleId(vehicleId);
            event.setLat(lat);
            event.setLng(lng);
            event.setSpeed(speed);
            event.setFuelLevel(fuelLevel);
            event.setTime(System.currentTimeMillis());

            listenerClient.emit(event);
            try {
                Thread.sleep(random.nextInt(10) * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
