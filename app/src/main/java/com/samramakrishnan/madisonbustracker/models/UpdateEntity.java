package com.samramakrishnan.madisonbustracker.models;

public class UpdateEntity {
    private String alert = null;
    private String id;
    TripUpdate trip_update;
    private String vehicle = null;

    @Override
    public String toString() {
        return "UpdateEntity{" +
                "alert='" + alert + '\'' +
                ", id='" + id + '\'' +
                ", trip_update=" + trip_update +
                ", vehicle='" + vehicle + '\'' +
                '}';
    }

// Getter Methods

    public String getAlert() {
        return alert;
    }

    public String getId() {
        return id;
    }

    public TripUpdate getTrip_update() {
        return trip_update;
    }

    public String getVehicle() {
        return vehicle;
    }

    // Setter Methods

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTrip_update(TripUpdate trip_updateObject) {
        this.trip_update = trip_updateObject;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
}



