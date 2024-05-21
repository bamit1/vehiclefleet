package com.vehiclefleet;

import com.vehiclefleet.client.ListenerClient;
import com.vehiclefleet.models.FleetUpdateEvent;

import java.sql.Timestamp;
import java.util.Random;

public class FleetEventGenerator implements Runnable {

    private final String vehicleId;
    private final ListenerClient listenerClient;

    public FleetEventGenerator(String vehicleId, ListenerClient listenerClient) {
        this.vehicleId = vehicleId;
        this.listenerClient = listenerClient;
    }

    @Override
    public void run() {
        System.out.println("started thread for vehicle: " + vehicleId);
        Random random = new Random();
        while (true) {
            FleetUpdateEvent event = new FleetUpdateEvent();
            event.setVehicleId(vehicleId);
            event.setLat(random.nextInt(-85, 84) + random.nextDouble());
            event.setLng(random.nextInt(-180, 179) + random.nextDouble());
            event.setSpeed(random.nextInt(100));
            event.setFuelLevel(random.nextInt(100));
            event.setTime(new Timestamp(System.currentTimeMillis()));

            listenerClient.emit(event);
            try {
                Thread.sleep(random.nextInt(10) * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
