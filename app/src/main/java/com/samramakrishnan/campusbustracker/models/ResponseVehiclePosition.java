package com.samramakrishnan.campusbustracker.models;

import java.util.ArrayList;

public class ResponseVehiclePosition {
    ArrayList< TripEntity > entity = new ArrayList < TripEntity >  ();
    Header HeaderObject;


    // Getter Methods

    public Header getHeader() {
        return HeaderObject;
    }

    // Setter Methods

    public void setHeader(Header headerObject) {
        this.HeaderObject = headerObject;
    }
}
 class Header {
    private float incrementality;
    private float timestamp;


    // Getter Methods

    public float getIncrementality() {
        return incrementality;
    }

    public float getTimestamp() {
        return timestamp;
    }

    // Setter Methods

    public void setIncrementality(float incrementality) {
        this.incrementality = incrementality;
    }

    public void setTimestamp(float timestamp) {
        this.timestamp = timestamp;
    }
}
