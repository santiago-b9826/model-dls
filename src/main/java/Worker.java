package main.java;

import java.util.Date;

public class Worker {
    private double seed;
    private Date startTime;
    private Date endTime;

    public Worker(double seed) {
        this.seed = seed;
    }

    public double getSeed() {
        return seed;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
