package com.example.processsim.algorithms;

import java.awt.Color;
import java.util.*;

/**
 * Base class for all scheduling algorithms.
 */
public abstract class SchedulingAlgorithm {
    
    protected static final Color[] GANTT_COLORS = {
        new Color(231, 76, 60),   // Red
        new Color(52, 152, 219),  // Blue
        new Color(46, 204, 113),  // Green
        new Color(155, 89, 182),  // Purple
        new Color(241, 196, 15),  // Yellow
        new Color(230, 126, 34),  // Orange
        new Color(26, 188, 156),  // Teal
        new Color(52, 73, 94)     // Dark Blue
    };

    protected List<Proc> processes;
    protected int quantum;

    public SchedulingAlgorithm(List<Proc> processes) {
        this(processes, 4);
    }

    public SchedulingAlgorithm(List<Proc> processes, int quantum) {
        this.processes = processes.stream().map(Proc::copy).toList();
        this.quantum = Math.max(1, quantum);
    }

    /**
     * Run the scheduling algorithm and return the result.
     */
    public abstract SimResult run();

    /**
     * Get the name of this algorithm.
     */
    public abstract String getName();

    /**
     * Build the final result with statistics.
     */
    protected SimResult buildResult(List<Proc> procs, List<GanttEntry> timeline) {
        SimResult result = new SimResult();
        result.timeline = timeline;
        result.colorMap = new HashMap<>();

        // Assign colors to processes
        int colorIndex = 0;
        for (Proc p : procs) {
            if (!result.colorMap.containsKey(p.name)) {
                result.colorMap.put(p.name, GANTT_COLORS[colorIndex++ % GANTT_COLORS.length]);
            }
        }

        // Build statistics text
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("  ").append(getName()).append("\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append(String.format("%-8s %-8s %-8s %-8s %-8s %-10s%n", 
            "Name", "Arrival", "Burst", "Finish", "Wait", "Turnaround"));
        sb.append("─".repeat(55)).append("\n");

        double totalWait = 0, totalTurnaround = 0;
        List<Proc> sorted = new ArrayList<>(procs);
        sorted.sort(Comparator.comparingInt(p -> p.arrival));

        for (Proc p : sorted) {
            int turnaround = p.finish - p.arrival;
            int wait = turnaround - p.burst;
            totalWait += wait;
            totalTurnaround += turnaround;
            sb.append(String.format("%-8s %-8d %-8d %-8d %-8d %-10d%n",
                p.name, p.arrival, p.burst, p.finish, wait, turnaround));
        }

        sb.append("─".repeat(55)).append("\n");
        sb.append(String.format("%nAverage Waiting Time:    %.2f%n", totalWait / procs.size()));
        sb.append(String.format("Average Turnaround Time: %.2f%n", totalTurnaround / procs.size()));

        result.text = sb.toString();
        return result;
    }
}
