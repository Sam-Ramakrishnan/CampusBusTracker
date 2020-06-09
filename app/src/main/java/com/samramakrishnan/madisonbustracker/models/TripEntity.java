package com.samramakrishnan.madisonbustracker.models;

public class TripEntity {
    private String alert = null;
    private String id;
    private String trip_update = null;
    Vehicle vehicle;

    @Override
    public String toString() {
        return "TripEntity{" +
                "alert='" + alert + '\'' +
                ", id='" + id + '\'' +
                ", trip_update='" + trip_update + '\'' +
                ", vehicle=" + vehicle +
                '}';
    }

// Getter Methods

    public String getAlert() {
        return alert;
    }

    public String getId() {
        return id;
    }

    public String getTrip_update() {
        return trip_update;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    // Setter Methods

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTrip_update(String trip_update) {
        this.trip_update = trip_update;
    }

    public void setVehicle(Vehicle vehicleObject) {
        this.vehicle = vehicleObject;
    }
}
