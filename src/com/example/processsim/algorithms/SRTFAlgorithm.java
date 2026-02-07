package com.example.processsim.algorithms;

import java.util.*;

/**
 * Shortest Remaining Time First (SRTF) scheduling algorithm.
 * Preemptive version of SJF: always runs the process with the least remaining time.
 */
public class SRTFAlgorithm extends SchedulingAlgorithm {

    public SRTFAlgorithm(List<Proc> processes) {
        super(processes);
    }

    @Override
    public String getName() {
        return "Shortest Remaining Time First (SRTF)";
    }

    @Override
    public SimResult run() {
        List<Proc> procs = new ArrayList<>(processes);
        List<GanttEntry> timeline = new ArrayList<>();

        int time = 0;
        int completed = 0;
        Proc current = null;
        int currentStart = 0;

        while (completed < procs.size()) {
            // Find process with shortest remaining time among arrived processes
            Proc shortest = null;
            for (Proc p : procs) {
                if (p.arrival <= time && p.remaining > 0) {
                    if (shortest == null || p.remaining < shortest.remaining) {
                        shortest = p;
                    }
                }
            }

            if (shortest == null) {
                // No process available, advance time
                if (current != null) {
                    timeline.add(new GanttEntry(current.name, currentStart, time));
                    current = null;
                }
                time++;
                continue;
            }

            // Context switch if different process
            if (current != shortest) {
                if (current != null) {
                    timeline.add(new GanttEntry(current.name, currentStart, time));
                }
                current = shortest;
                currentStart = time;
                if (current.start == -1) {
                    current.start = time;
                }
            }

            // Execute for 1 time unit
            current.remaining--;
            time++;

            // Check if process completed
            if (current.remaining == 0) {
                current.finish = time;
                timeline.add(new GanttEntry(current.name, currentStart, time));
                current = null;
                completed++;
            }
        }

        return buildResult(procs, timeline);
    }
}
