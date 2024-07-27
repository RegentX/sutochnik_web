package org.example.sutochnikweb.models;

public class OperationStats {
    private int count;
    private double totalDuration;

    public OperationStats() {
        this.count = 0;
        this.totalDuration = 0;
    }

    public void incrementCount() {
        this.count++;
    }

    public void addDuration(double duration) {
        this.totalDuration += duration;
    }

    public int getCount() {
        return count;
    }

    public double getTotalDuration() {
        return totalDuration;
    }
}