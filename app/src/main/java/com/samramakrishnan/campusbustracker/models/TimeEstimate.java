package com.samramakrishnan.campusbustracker.models;

public class TimeEstimate implements Comparable {
   private float delay;
   private long time;

    @Override
    public String toString() {
        return "TimeEstimate{" +
                "delay=" + delay +
                ", time=" + time +
                '}';
    }

// Getter Methods

   public float getDelay() {
       return delay;
   }

   public long getTime() {
       return time;
   }

   // Setter Methods

   public void setDelay(float delay) {
       this.delay = delay;
   }

   public void setTime(long time) {
       this.time = time;
   }



    @Override
    public int compareTo(Object o) {
       if(o instanceof TimeEstimate){
           if(time>((TimeEstimate) o).time){
               return 1;
           }
       }
        return -1;
    }
}
