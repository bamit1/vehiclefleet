package com.vehiclefleet;

import com.vehiclefleet.client.ListenerClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {

    private static final ListenerClient listenerClient = new ListenerClient();

    public static void main(String[] args) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("starting to send events!");
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 1; i <= 5; i++) {
            String vehicleId = "Vehicle-" + i;
            FleetEventGenerator generator = new FleetEventGenerator(vehicleId, listenerClient);
            executorService.submit(generator);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down executor service...");
            executorService.shutdown();
        }));
    }
}
