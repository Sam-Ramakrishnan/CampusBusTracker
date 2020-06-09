package com.samramakrishnan.madisonbustracker.models;

public class Position {
    private float latitude;
    private float longitude;


    // Getter Methods

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    // Setter Methods

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}