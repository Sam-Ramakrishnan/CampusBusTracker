package com.samramakrishnan.madisonbustracker.models;

public class VehicleInfo {
    private String id;
    private String label;

    @Override
    public String toString() {
        return "VehicleInfo{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
    }

// Getter Methods

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    // Setter Methods

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}