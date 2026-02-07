package com.example.processsim.algorithms;

import java.util.*;

/**
 * Multilevel Feedback Queue (MLFQ) scheduling algorithm.
 *
 * 3 queues:
 *   Queue 0 – Round Robin with quantum = 2
 *   Queue 1 – Round Robin with quantum = 4
 *   Queue 2 – FCFS (runs to completion)
 *
 * Rules:
 *   • New processes enter Queue 0.
 *   • If a process uses its full quantum without finishing, it is demoted
 *     to the next lower-priority queue.
 *   • Higher-priority queues are always served first (preemptive between queues).
 *   • Aging: if a process waits in Queue 1 or Queue 2 for ≥ AGING_THRESHOLD
 *     time units without running, it is promoted one level to prevent starvation.
 */
public class MLFQAlgorithm extends SchedulingAlgorithm {

    private static final int NUM_QUEUES = 3;
    private static final int[] QUANTA = {2, 4, Integer.MAX_VALUE}; // Q2 = FCFS
    private static final int AGING_THRESHOLD = 10;

    public MLFQAlgorithm(List<Proc> processes) {
        super(processes);
    }

    @Override
    public String getName() {
        return "MLFQ [Q=2, 4, FCFS]";
    }

    /* ── tiny helper to track per-process MLFQ state ── */
    private static class MlfqProc {
        Proc proc;
        int queueLevel;          // 0, 1, or 2
        int lastRunTime;         // last time this process was executed

        MlfqProc(Proc p, int arrivalTime) {
            this.proc = p;
            this.queueLevel = 0; // all new arrivals enter queue 0
            this.lastRunTime = arrivalTime;
        }
    }

    @Override
    public SimResult run() {
        List<Proc> procs = new ArrayList<>(processes);
        procs.sort(Comparator.comparingInt(p -> p.arrival));

        // Wrap each Proc with MLFQ metadata
        List<MlfqProc> allMlfq = new ArrayList<>();
        for (Proc p : procs) {
            allMlfq.add(new MlfqProc(p, p.arrival));
        }

        // Three ready queues (FIFO order inside each)
        @SuppressWarnings("unchecked")
        Queue<MlfqProc>[] queues = new LinkedList[NUM_QUEUES];
        for (int i = 0; i < NUM_QUEUES; i++) {
            queues[i] = new LinkedList<>();
        }

        List<GanttEntry> timeline = new ArrayList<>();
        int time = 0;
        int admitted = 0;  // index into sorted procs
        int completed = 0;

        while (completed < procs.size()) {

            // 1. Admit newly arrived processes into Queue 0
            while (admitted < allMlfq.size() && allMlfq.get(admitted).proc.arrival <= time) {
                queues[0].add(allMlfq.get(admitted));
                admitted++;
            }

            // 2. Aging – promote starving processes
            applyAging(queues, time);

            // 3. Pick the highest-priority non-empty queue
            int level = -1;
            for (int i = 0; i < NUM_QUEUES; i++) {
                if (!queues[i].isEmpty()) {
                    level = i;
                    break;
                }
            }

            if (level == -1) {
                // CPU idle – fast-forward to next arrival
                if (admitted < allMlfq.size()) {
                    time = allMlfq.get(admitted).proc.arrival;
                    continue;
                } else {
                    break; // nothing left
                }
            }

            MlfqProc mp = queues[level].poll();
            Proc p = mp.proc;

            if (p.start == -1) {
                p.start = time;
            }

            int quantum = QUANTA[level];
            int runTime = Math.min(quantum, p.remaining);

            // Execute – but we must also check for higher-priority preemption
            // For simplicity (and clarity), we run the full slice then re-evaluate.
            // Within the slice we still admit arrivals so they are queued correctly.
            timeline.add(new GanttEntry(p.name, time, time + runTime));
            p.remaining -= runTime;
            time += runTime;
            mp.lastRunTime = time;

            // Admit processes that arrived during execution
            while (admitted < allMlfq.size() && allMlfq.get(admitted).proc.arrival <= time) {
                queues[0].add(allMlfq.get(admitted));
                admitted++;
            }

            if (p.remaining == 0) {
                // Process finished
                p.finish = time;
                completed++;
            } else if (runTime >= quantum && level < NUM_QUEUES - 1) {
                // Used full quantum → demote
                mp.queueLevel = level + 1;
                queues[mp.queueLevel].add(mp);
            } else {
                // Did not use full quantum (preempted by higher queue after re-evaluation)
                // Stay in same queue
                queues[level].add(mp);
            }
        }

        return buildResult(procs, timeline);
    }

    /**
     * Promote any process that has been waiting ≥ AGING_THRESHOLD time units
     * without running, moving it one queue level up.
     */
    private void applyAging(Queue<MlfqProc>[] queues, int currentTime) {
        for (int level = 1; level < NUM_QUEUES; level++) {
            Iterator<MlfqProc> it = queues[level].iterator();
            List<MlfqProc> promoted = new ArrayList<>();
            while (it.hasNext()) {
                MlfqProc mp = it.next();
                if (currentTime - mp.lastRunTime >= AGING_THRESHOLD) {
                    it.remove();
                    mp.queueLevel = level - 1;
                    mp.lastRunTime = currentTime; // reset aging clock
                    promoted.add(mp);
                }
            }
            // Add promoted processes to the end of the higher-priority queue
            queues[level - 1].addAll(promoted);
        }
    }
}
