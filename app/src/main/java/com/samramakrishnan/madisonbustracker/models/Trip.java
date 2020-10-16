package com.samramakrishnan.madisonbustracker.models;

public class Trip {

    private String trip_id;
    private String start_time;
    private String start_date;
    private float schedule_relationship;
    private String route_id;
    private float direction_id;

    @Override
    public String toString() {
        return "Trip{" +
                "route_id='" + route_id + '\'' +
//                ", schedule_relationship=" + schedule_relationship +
                ", start_date='" + start_date + '\'' +
                ", trip_id='" + trip_id + '\'' +
                '}';
    }

// Getter Methods

    public String getRoute_id() {
        return route_id;
    }

//    public float getSchedule_relationship() {
//        return schedule_relationship;
//    }

    public String getStart_date() {
        return start_date;
    }

    public String getTrip_id() {
        return trip_id;
    }

    // Setter Methods

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

//    public void setSchedule_relationship(float schedule_relationship) {
//        this.schedule_relationship = schedule_relationship;
//    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }
}