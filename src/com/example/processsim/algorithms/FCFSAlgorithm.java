package com.example.processsim.algorithms;

import java.util.*;

/**
 * First Come First Served (FCFS) scheduling algorithm.
 * Processes are executed in the order they arrive.
 */
public class FCFSAlgorithm extends SchedulingAlgorithm {

    public FCFSAlgorithm(List<Proc> processes) {
        super(processes);
    }

    @Override
    public String getName() {
        return "First Come First Served (FCFS)";
    }

    @Override
    public SimResult run() {
        List<Proc> procs = new ArrayList<>(processes);
        procs.sort(Comparator.comparingInt(p -> p.arrival));

        List<GanttEntry> timeline = new ArrayList<>();
        int time = 0;

        for (Proc p : procs) {
            // If CPU is idle, jump to process arrival
            if (time < p.arrival) {
                time = p.arrival;
            }

            p.start = time;
            timeline.add(new GanttEntry(p.name, time, time + p.burst));
            time += p.burst;
            p.finish = time;
        }

        return buildResult(procs, timeline);
    }
}
