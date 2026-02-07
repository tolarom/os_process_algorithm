package com.example.processsim.algorithms;

import java.util.*;

/**
 * Round Robin scheduling algorithm.
 * Processes are executed in circular order with a fixed time quantum.
 */
public class RoundRobinAlgorithm extends SchedulingAlgorithm {

    public RoundRobinAlgorithm(List<Proc> processes, int quantum) {
        super(processes, quantum);
    }

    @Override
    public String getName() {
        return "Round Robin (Q=" + quantum + ")";
    }

    @Override
    public SimResult run() {
        List<Proc> procs = new ArrayList<>(processes);
        procs.sort(Comparator.comparingInt(p -> p.arrival));

        List<GanttEntry> timeline = new ArrayList<>();
        Queue<Proc> queue = new LinkedList<>();
        int time = 0;
        int index = 0;

        while (true) {
            // Add all processes that have arrived
            while (index < procs.size() && procs.get(index).arrival <= time) {
                queue.add(procs.get(index++));
            }

            if (queue.isEmpty()) {
                if (index < procs.size()) {
                    // Jump to next arrival time
                    time = procs.get(index).arrival;
                    continue;
                } else {
                    break; // All done
                }
            }

            Proc p = queue.poll();
            if (p.start == -1) {
                p.start = time;
            }

            int runTime = Math.min(quantum, p.remaining);
            timeline.add(new GanttEntry(p.name, time, time + runTime));
            p.remaining -= runTime;
            time += runTime;

            // Add newly arrived processes during this execution
            while (index < procs.size() && procs.get(index).arrival <= time) {
                queue.add(procs.get(index++));
            }

            if (p.remaining > 0) {
                queue.add(p); // Re-queue if not finished
            } else {
                p.finish = time;
            }
        }

        return buildResult(procs, timeline);
    }
}
