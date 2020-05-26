package com.samramakrishnan.campusbustracker.models;

import java.util.ArrayList;

public class ResponseVehiclePosition {
    ArrayList< TripEntity > entity = new ArrayList < TripEntity >  ();
    Header header;


    // Getter Methods

    public ArrayList<TripEntity> getEntity() {
        return entity;
    }

    public Header getHeader() {
        return header;
    }

    // Setter Methods


    public void setEntity(ArrayList<TripEntity> entity) {
        this.entity = entity;
    }

    public void setHeader(Header headerObject) {
        this.header = headerObject;
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
