package com.example.processsim.algorithms;

/**
 * Represents a process in the scheduling simulation.
 */
public class Proc {
    public String name;
    public int arrival;
    public int burst;
    public int remaining;
    public int finish = -1;
    public int start = -1;
    public Proc(String name, int arrival, int burst) {
        this.name = name;
        this.arrival = arrival;
        this.burst = burst;
        this.remaining = burst;
    }

    public Proc copy() {
        return new Proc(name, arrival, burst);
    }
}
