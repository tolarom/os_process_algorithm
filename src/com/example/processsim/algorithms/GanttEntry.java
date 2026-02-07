package com.example.processsim.algorithms;

/**
 * Represents a single entry in the Gantt chart timeline.
 */
public class GanttEntry {
    public String name;
    public int start;
    public int end;

    public GanttEntry(String name, int start, int end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }
}
