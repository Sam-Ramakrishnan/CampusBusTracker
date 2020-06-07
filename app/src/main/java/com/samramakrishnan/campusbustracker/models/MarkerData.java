package com.samramakrishnan.campusbustracker.models;

public class MarkerData {
    Stop stop;
    TimeEstimate timeEstimate;

    public MarkerData(Stop stop, TimeEstimate timeEstimate) {
        this.stop = stop;
        this.timeEstimate = timeEstimate;
    }

    public Stop getStop() {
        return stop;
    }

    public TimeEstimate getTimeEstimate() {
        return timeEstimate;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public void setTimeEstimate(TimeEstimate timeEstimate) {
        this.timeEstimate = timeEstimate;
    }
}




