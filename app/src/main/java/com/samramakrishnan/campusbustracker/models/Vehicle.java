package com.samramakrishnan.campusbustracker.models;

public class Vehicle {
    Position position;
    private float timestamp;
    Trip trip;
    VehicleInfo vehicle;


    // Getter Methods

    public Position getPosition() {
        return position;
    }

    public float getTimestamp() {
        return timestamp;
    }

    public Trip getTrip() {
        return trip;
    }

    public VehicleInfo getVehicle() {
        return vehicle;
    }

    // Setter Methods

    public void setPosition(Position positionObject) {
        this.position = positionObject;
    }

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



