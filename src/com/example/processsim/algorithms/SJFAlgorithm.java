package com.example.processsim.algorithms;

import java.util.*;

/**
 * Shortest Job First (SJF) scheduling algorithm.
 * Non-preemptive: selects the process with the shortest burst time.
 */
public class SJFAlgorithm extends SchedulingAlgorithm {

    public SJFAlgorithm(List<Proc> processes) {
        super(processes);
    }

    @Override
    public String getName() {
        return "Shortest Job First (SJF)";
    }

    @Override
    public SimResult run() {
        List<Proc> procs = new ArrayList<>(processes);
        List<GanttEntry> timeline = new ArrayList<>();
        List<Proc> ready = new ArrayList<>();

        int time = 0;
        int completed = 0;

        while (completed < procs.size()) {
            // Add all arrived processes to ready queue
            for (Proc p : procs) {
                if (p.arrival <= time && p.finish == -1 && !ready.contains(p)) {
                    ready.add(p);
                }
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            // Select process with shortest burst time
            ready.sort(Comparator.comparingInt(p -> p.burst));
            Proc p = ready.remove(0);

            p.start = time;
            timeline.add(new GanttEntry(p.name, time, time + p.burst));
            time += p.burst;
            p.finish = time;
            completed++;
        }

        return buildResult(procs, timeline);
    }
}
