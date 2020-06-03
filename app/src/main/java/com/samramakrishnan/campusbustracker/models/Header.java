package com.samramakrishnan.campusbustracker.models;

public class Header {
    private float incrementality;
    private float timestamp;

    @Override
    public String toString() {
        return "Header{" +
                "incrementality=" + incrementality +
                ", timestamp=" + timestamp +
                '}';
    }

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