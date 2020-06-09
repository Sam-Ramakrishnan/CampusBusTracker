package com.samramakrishnan.madisonbustracker.models;

import java.util.ArrayList;

public class ResponseVehiclePosition {
    ArrayList< TripEntity > entity = new ArrayList < TripEntity >  ();
    Header header;

    @Override
    public String toString() {
        return "ResponseVehiclePosition{" +
                "entity=" + entity +
                ", header=" + header +
                '}';
    }

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

