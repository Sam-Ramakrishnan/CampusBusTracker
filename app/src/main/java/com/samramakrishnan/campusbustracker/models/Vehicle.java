package com.samramakrishnan.campusbustracker.models;

public class Vehicle {
    Position PositionObject;
    private float timestamp;
    Trip TripObject;
    Vehicle VehicleObject;


    // Getter Methods

    public Position getPosition() {
        return PositionObject;
    }

    public float getTimestamp() {
        return timestamp;
    }

    public Trip getTrip() {
        return TripObject;
    }

    public Vehicle getVehicle() {
        return VehicleObject;
    }

    // Setter Methods

    public void setPosition(Position positionObject) {
        this.PositionObject = positionObject;
    }

    public void setTimestamp(float timestamp) {
        this.timestamp = timestamp;
    }

    public void setTrip(Trip tripObject) {
        this.TripObject = tripObject;
    }

    public void setVehicle(Vehicle vehicleObject) {
        this.VehicleObject = vehicleObject;
    }
}



