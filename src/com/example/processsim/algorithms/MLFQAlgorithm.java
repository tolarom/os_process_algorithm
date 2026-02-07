package com.example.processsim.algorithms;

import java.util.*;

/**
 * Multi-Level Feedback Queue (MLFQ) scheduling algorithm.
 * 
 * Uses multiple queues with different priorities and time quantums:
 * - Queue 0 (highest priority): quantum = 4
 * - Queue 1 (medium priority): quantum = 8
 * - Queue 2 (lowest priority): FCFS (runs to completion)
 * 
 * New processes start at their initial priority queue (0-2).
 * If a process uses its full quantum, it moves down to a lower priority queue.
 */
public class MLFQAlgorithm extends SchedulingAlgorithm {

    private static final int NUM_QUEUES = 3;
    private static final int[] QUANTUMS = {4, 8, Integer.MAX_VALUE}; // Q0, Q1, Q2 (FCFS)

    public MLFQAlgorithm(List<Proc> processes) {
        super(processes);
    }

    @Override
    public String getName() {
        return "Multi-Level Feedback Queue (MLFQ)";
    }

    @Override
    public SimResult run() {
        List<Proc> procs = new ArrayList<>(processes.stream().map(Proc::copy).toList());
        procs.sort(Comparator.comparingInt(p -> p.arrival));

        List<GanttEntry> timeline = new ArrayList<>();
        
        // Create queues for each level
        @SuppressWarnings("unchecked")
        Queue<Proc>[] queues = new LinkedList[NUM_QUEUES];
        for (int i = 0; i < NUM_QUEUES; i++) {
            queues[i] = new LinkedList<>();
        }
        
        // Track which queue each process is in
        Map<Proc, Integer> queueLevel = new HashMap<>();
        
        int time = 0;
        int index = 0; // Index for adding arriving processes
        int completed = 0;

        while (completed < procs.size()) {
            // Add newly arrived processes to their initial priority queue
            while (index < procs.size() && procs.get(index).arrival <= time) {
                Proc p = procs.get(index++);
                int initialQueue = Math.min(p.priority, NUM_QUEUES - 1); // Ensure valid queue (0-2)
                queues[initialQueue].add(p);
                queueLevel.put(p, initialQueue);
            }

            // Find the highest priority non-empty queue
            Proc currentProc = null;
            int currentQueue = -1;
            for (int q = 0; q < NUM_QUEUES; q++) {
                if (!queues[q].isEmpty()) {
                    currentProc = queues[q].poll();
                    currentQueue = q;
                    break;
                }
            }

            if (currentProc == null) {
                // No process ready, advance time
                if (index < procs.size()) {
                    time = procs.get(index).arrival;
                }
                continue;
            }

            // Record start time
            if (currentProc.start == -1) {
                currentProc.start = time;
            }

            // Determine how long to run
            int quantum = QUANTUMS[currentQueue];
            int runTime = Math.min(quantum, currentProc.remaining);
            
            // Check if a higher priority process will arrive during execution
            int nextArrival = Integer.MAX_VALUE;
            if (index < procs.size()) {
                nextArrival = procs.get(index).arrival;
            }
            
            // For queues 0 and 1, check for preemption by new arrivals
            if (currentQueue > 0 && nextArrival < time + runTime) {
                runTime = nextArrival - time;
            }

            // Execute the process
            timeline.add(new GanttEntry(currentProc.name, time, time + runTime));
            currentProc.remaining -= runTime;
            time += runTime;

            // Add any processes that arrived during execution to their initial priority queue
            while (index < procs.size() && procs.get(index).arrival <= time) {
                Proc p = procs.get(index++);
                int initialQueue = Math.min(p.priority, NUM_QUEUES - 1); // Ensure valid queue (0-2)
                queues[initialQueue].add(p);
                queueLevel.put(p, initialQueue);
            }

            // Handle process completion or demotion
            if (currentProc.remaining == 0) {
                currentProc.finish = time;
                completed++;
            } else {
                // Process didn't finish
                if (runTime >= QUANTUMS[currentQueue] && currentQueue < NUM_QUEUES - 1) {
                    // Used full quantum, demote to lower priority queue
                    int newQueue = currentQueue + 1;
                    queues[newQueue].add(currentProc);
                    queueLevel.put(currentProc, newQueue);
                } else {
                    // Was preempted, stay in same queue
                    queues[currentQueue].add(currentProc);
                }
            }
        }

        // Merge consecutive entries for the same process
        List<GanttEntry> mergedTimeline = mergeTimeline(timeline);
        
        return buildResult(procs, mergedTimeline);
    }
    
    private List<GanttEntry> mergeTimeline(List<GanttEntry> timeline) {
        if (timeline.isEmpty()) return timeline;
        
        List<GanttEntry> merged = new ArrayList<>();
        GanttEntry current = timeline.get(0);
        
        for (int i = 1; i < timeline.size(); i++) {
            GanttEntry next = timeline.get(i);
            if (next.name.equals(current.name) && next.start == current.end) {
                // Merge consecutive entries for same process
                current = new GanttEntry(current.name, current.start, next.end);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        
        return merged;
    }
}
