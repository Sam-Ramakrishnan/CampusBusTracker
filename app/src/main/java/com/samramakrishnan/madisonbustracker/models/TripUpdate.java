package com.samramakrishnan.madisonbustracker.models;

import java.util.ArrayList;

public class TripUpdate {
    ArrayList<StopTimeUpdate> stop_time_update = new ArrayList<>();
    private float timestamp;
    Trip trip;
    VehicleInfo vehicle;

    @Override
    public String toString() {
        return "TripUpdate{" +
                "stop_time_update=" + stop_time_update +
                ", timestamp=" + timestamp +
                ", trip=" + trip +
                ", vehicle=" + vehicle +
                '}';
    }

// Getter Methods

    public float getTimestamp() {
        return timestamp;
    }

    public Trip getTrip() {
        return trip;
    }

    public VehicleInfo getVehicle() {
        return vehicle;
    }

    public ArrayList<StopTimeUpdate> getStop_time_update() {
        return stop_time_update;
    }
    // Setter Methods

    public void setTimestamp(float timestamp) {
        this.timestamp = timestamp;
    }

    public void setTrip(Trip tripObject) {
        this.trip = tripObject;
    }

    public void setVehicle(VehicleInfo vehicleObject) {
        this.vehicle = vehicleObject;
    }
}

