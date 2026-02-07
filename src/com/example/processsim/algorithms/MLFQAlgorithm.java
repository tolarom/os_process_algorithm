package com.example.processsim.algorithms;

import java.util.*;


public class MLFQAlgorithm extends SchedulingAlgorithm {

    private static final int NUM_QUEUES = 3;
    private final int q0;
    private final int q1;
    private static final int AGING_THRESHOLD = 10;

    public MLFQAlgorithm(List<Proc> processes) {
        this(processes, 2, 4);
    }

    public MLFQAlgorithm(List<Proc> processes, int q0, int q1) {
        super(processes);
        this.q0 = Math.max(1, q0);
        this.q1 = Math.max(1, q1);
    }

    @Override
    public String getName() {
        return "MLFQ [Q0=" + q0 + ", Q1=" + q1 + ", Q2=FCFS]";
    }

    /* ── tiny helper to track per-process MLFQ state ── */
    private static class MlfqProc {
        Proc proc;
        int queueLevel;         
        int lastRunTime;
        int queueEnterTime;  // When process entered current queue

        MlfqProc(Proc p, int arrivalTime) {
            this.proc = p;
            this.queueLevel = 0; 
            this.lastRunTime = arrivalTime;
            this.queueEnterTime = arrivalTime;
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
        int admitted = 0;  
        int completed = 0;

        while (completed < procs.size()) {

            // 1. Admit newly arrived processes into Queue 0
            while (admitted < allMlfq.size() && allMlfq.get(admitted).proc.arrival <= time) {
                MlfqProc newProc = allMlfq.get(admitted);
                newProc.queueEnterTime = time; // Set entry time when actually admitted
                queues[0].add(newProc);
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

            int quantum = switch (level) {
                case 0 -> q0;
                case 1 -> q1;
                default -> Integer.MAX_VALUE; // Q2 = FCFS
            };
            int runTime = Math.min(quantum, p.remaining);

            timeline.add(new GanttEntry(p.name, time, time + runTime));
            p.remaining -= runTime;
            time += runTime;
            mp.lastRunTime = time;

            // Admit processes that arrived during execution
            while (admitted < allMlfq.size() && allMlfq.get(admitted).proc.arrival <= time) {
                MlfqProc newProc = allMlfq.get(admitted);
                newProc.queueEnterTime = time; // Set entry time when actually admitted
                queues[0].add(newProc);
                admitted++;
            }

            if (p.remaining == 0) {
                // Process finished
                p.finish = time;
                completed++;
            } else if (runTime == quantum && level < NUM_QUEUES - 1) {
                // Used full quantum without finishing → demote
                mp.queueLevel = level + 1;
                mp.queueEnterTime = time;  // Record when entered new queue
                queues[mp.queueLevel].add(mp);
            } else {
                // Didn't use full quantum → stay in same queue
                // Don't update queueEnterTime - it continues waiting from same position
                queues[level].add(mp);
            }
        }

        return buildResult(procs, timeline);
    }

    private void applyAging(Queue<MlfqProc>[] queues, int currentTime) {
        for (int level = 1; level < NUM_QUEUES; level++) {
            // Only apply aging if there are processes in higher-priority queues
            // (to prevent unnecessary promotion when queue is about to run anyway)
            boolean hasHigherPriority = false;
            for (int i = 0; i < level; i++) {
                if (!queues[i].isEmpty()) {
                    hasHigherPriority = true;
                    break;
                }
            }
            
            if (!hasHigherPriority) {
                continue; // Skip aging for this level if no higher priority work
            }
            
            Iterator<MlfqProc> it = queues[level].iterator();
            List<MlfqProc> promoted = new ArrayList<>();
            while (it.hasNext()) {
                MlfqProc mp = it.next();
                // Use queueEnterTime for aging calculation instead of lastRunTime
                if (currentTime - mp.queueEnterTime > AGING_THRESHOLD) {
                    it.remove();
                    mp.queueLevel = level - 1;
                    mp.queueEnterTime = currentTime; // reset aging clock
                    promoted.add(mp);
                }
            }
            // Add promoted processes to the end of the higher-priority queue
            queues[level - 1].addAll(promoted);
        }
    }
}
