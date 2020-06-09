package com.samramakrishnan.madisonbustracker.models;

import java.util.ArrayList;

public class ResponseTripUpdate {
    ArrayList< UpdateEntity > entity = new ArrayList < > ();
    Header header;

    @Override
    public String toString() {
        return "ResponseTripUpdate{" +
                "entity=" + entity +
                ", header=" + header +
                '}';
    }
// Getter Methods

    public Header getHeader() {
        return header;
    }

    public ArrayList<UpdateEntity> getEntity() {
        return entity;
    }

    // Setter Methods

    public void setHeader(Header headerObject) {
        this.header = headerObject;
    }

    public void setEntity(ArrayList<UpdateEntity> entity) {
        this.entity = entity;
    }
}
