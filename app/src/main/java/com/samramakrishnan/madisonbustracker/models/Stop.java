package com.samramakrishnan.madisonbustracker.models;

public class Stop implements Comparable {
    String id, name;
    double lat, longi;

    public Stop(String id, String name, double lat, double longi) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.longi = longi;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    @Override
    public String toString() {
        return "Stop{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", longi=" + longi +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
