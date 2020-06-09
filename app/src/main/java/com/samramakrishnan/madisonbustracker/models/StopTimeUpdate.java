package com.samramakrishnan.madisonbustracker.models;

public class StopTimeUpdate {
    private TimeEstimate arrival;
    private TimeEstimate departure;
    private float schedule_relationship;
    private String stop_id;
    private float stop_sequence;

    @Override
    public String toString() {
        return "StopTimeUpdate{" +
                "arrival=" + arrival +
                ", departure=" + departure +
                ", schedule_relationship=" + schedule_relationship +
                ", stop_id='" + stop_id + '\'' +
                ", stop_sequence=" + stop_sequence +
                '}';
    }

// Getter Methods

    public TimeEstimate getArrival() {
        return arrival;
    }

    public TimeEstimate getDeparture() {
        return departure;
    }

    public float getSchedule_relationship() {
        return schedule_relationship;
    }

    public String getStop_id() {
        return stop_id;
    }

    public float getStop_sequence() {
        return stop_sequence;
    }

    // Setter Methods

    public void setArrival(TimeEstimate arrival) {
        this.arrival = arrival;
    }

    public void setDeparture(TimeEstimate departureObject) {
        this.departure = departureObject;
    }

    public void setSchedule_relationship(float schedule_relationship) {
        this.schedule_relationship = schedule_relationship;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public void setStop_sequence(float stop_sequence) {
        this.stop_sequence = stop_sequence;
    }
}
