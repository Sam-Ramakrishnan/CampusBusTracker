package com.samramakrishnan.madisonbustracker.models;

public class Vehicle {
    Position position;

    Trip trip;
    VehicleInfo vehicle;


    private float current_stop_sequence;
    private float current_status;
    private float timestamp;
    private float congestion_level;
    private String stop_id;
    private float occupancy_status;
    private float occupancy_percentage;

    @Override
    public String toString() {
        return "Vehicle{" +
                "position=" + position +
                ", trip=" + trip +
                ", vehicle=" + vehicle +
                ", current_stop_sequence=" + current_stop_sequence +
                ", current_status=" + current_status +
                ", timestamp=" + timestamp +
                ", congestion_level=" + congestion_level +
                ", stop_id='" + stop_id + '\'' +
                ", occupancy_status=" + occupancy_status +
                ", occupancy_percentage=" + occupancy_percentage +
                '}';
    }

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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTrip(Trip tripObject) {
        this.trip = tripObject;
    }

    public void setVehicle(VehicleInfo vehicleObject) {
        this.vehicle = vehicleObject;
    }
}



