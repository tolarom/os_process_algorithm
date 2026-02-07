# Process Scheduling Simulator

A Java-based GUI application for visualizing and comparing different CPU scheduling algorithms. This simulator provides an interactive interface to add processes, select scheduling algorithms, and visualize execution through Gantt charts.

## Features

- **Interactive GUI**: Modern Swing-based interface for easy process management
- **Multiple Scheduling Algorithms**: Supports 5 different CPU scheduling algorithms
- **Gantt Chart Visualization**: Real-time visualization of process execution
- **Performance Metrics**: Calculate and display turnaround time, waiting time, and response time
- **Process Management**: Add, edit, and delete processes with customizable parameters

## Setup Instructions

### Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher
- **Java Runtime Environment (JRE)**: Required to run the compiled program

### Installation

1. **Clone or download the repository**:
   ```bash
   git clone <repository-url>
   cd process_algorithm
   ```

2. **Compile the project**:
   ```bash
   javac -d bin src/com/example/processsim/*.java src/com/example/processsim/algorithms/*.java
   ```

   Alternative (compile all Java files at once):
   ```bash
   javac -d bin src/com/example/processsim/**/*.java
   ```

3. **Run the application**:
   ```bash
   java -cp bin com.example.processsim.ProcessSimulator
   ```

### Quick Run (Without Compilation)

If you have an IDE like IntelliJ IDEA, Eclipse, or VS Code with Java extension:
1. Open the project folder
2. Navigate to [ProcessSimulator.java](src/com/example/processsim/ProcessSimulator.java)
3. Run the `main` method

## Algorithms Implemented

### 1. First Come First Served (FCFS)

**Type**: Non-preemptive

**Description**: Processes are executed in the order they arrive in the ready queue. The first process to arrive is the first to be executed until completion.

**Characteristics**:
- Simple and easy to implement
- No starvation (every process gets executed eventually)
- Can suffer from the "convoy effect" where short processes wait for long processes
- Average waiting time can be high

**Best Use Case**: Batch systems with similar process lengths

---

### 2. Shortest Job First (SJF)

**Type**: Non-preemptive

**Description**: Selects the process with the shortest burst time from the ready queue. Once a process starts execution, it runs to completion.

**Characteristics**:
- Optimal for minimizing average waiting time
- Can cause starvation for longer processes
- Requires knowledge of burst times in advance
- Not suitable for interactive systems

**Best Use Case**: Batch processing where execution times are known

---

### 3. Shortest Remaining Time First (SRTF)

**Type**: Preemptive

**Description**: The preemptive version of SJF. Always executes the process with the shortest remaining burst time. If a new process arrives with a shorter remaining time than the currently executing process, the CPU is preempted.

**Characteristics**:
- Optimal for minimizing average waiting time among preemptive algorithms
- Higher context switching overhead
- Can cause starvation for longer processes
- Requires knowledge of burst times

**Best Use Case**: Time-sharing systems where minimizing response time is critical

---

### 4. Round Robin (RR)

**Type**: Preemptive

**Description**: Each process is assigned a fixed time quantum. Processes are executed in circular order, and if a process doesn't complete within its quantum, it's moved to the back of the queue.

**Characteristics**:
- Fair allocation of CPU time
- Good for time-sharing systems
- Performance depends on time quantum size
- No starvation
- Higher context switching overhead with small quantum

**Best Use Case**: Time-sharing and interactive systems

**Time Quantum**: Configurable (default values typically 2-4 time units)

---

### 5. Multi-Level Feedback Queue (MLFQ)

**Type**: Preemptive

**Description**: Uses 3 priority queues with increasing time quantums. All new processes enter Queue 0 (highest priority). If a process uses its full quantum without finishing, it is **demoted** to the next lower-priority queue. Higher-priority queues are always served first. An **aging** mechanism promotes processes that have been waiting too long, preventing starvation.

**Queue Structure**:
| Queue | Priority | Time Quantum | Scheduling |
|-------|----------|-------------|------------|
| Q0 | Highest | 2 | Round Robin |
| Q1 | Medium | 4 | Round Robin |
| Q2 | Lowest | ∞ | FCFS (runs to completion) |

**Promotion / Demotion Rules**:
- **Demotion**: A process that exhausts its full quantum is moved down one queue level (Q0 → Q1 → Q2).
- **Promotion (Aging)**: A process that has been waiting ≥ 10 time units without receiving CPU time is promoted up one queue level to prevent starvation.
- Processes that finish before their quantum expires stay at the same level (relevant if re-queued).

**Characteristics**:
- Adapts to process behavior — short / I/O-bound processes finish quickly in Q0
- Long CPU-bound processes gradually sink to Q2 (FCFS)
- Aging prevents indefinite starvation of lower-priority processes
- Good balance between response time and throughput
- No manual priority assignment required

**Best Use Case**: General-purpose operating systems with mixed workloads

## How to Run Each Scheduler

### Basic Steps

1. **Launch the application**:
   ```bash
   java -cp bin com.example.processsim.ProcessSimulator
   ```

2. **Add processes**:
   - Enter process details in the input form:
     - **Name**: Process identifier (e.g., P1, P2)
     - **Arrival Time**: When the process arrives in the ready queue
     - **Burst Time**: CPU time required by the process
   - Click **Add Process**

3. **Select algorithm**:
   - Choose from the dropdown menu:
     - First Come First Served (FCFS)
     - Shortest Job First (SJF)
     - Shortest Remaining Time First (SRTF)
     - Round Robin (RR)
     - Multi-Level Feedback Queue (MLFQ)

4. **Set parameters** (if applicable):
   - **Time Quantum**: Required for Round Robin (e.g., 2, 3, 4)

5. **Run simulation**:
   - Click **Run Scheduler**
   - View results in the Gantt Chart and output area

### Running Specific Schedulers

#### FCFS (First Come First Served)
```
1. Add processes with different arrival times
2. Select "First Come First Served (FCFS)" from dropdown
3. Click "Run Scheduler"
4. Observe processes executing in arrival order
```

#### SJF (Shortest Job First)
```
1. Add processes with varying burst times
2. Select "Shortest Job First (SJF)" from dropdown
3. Click "Run Scheduler"
4. Notice shortest jobs execute first (among arrived processes)
```

#### SRTF (Shortest Remaining Time First)
```
1. Add processes with different arrival times and burst times
2. Select "Shortest Remaining Time First (SRTF)" from dropdown
3. Click "Run Scheduler"
4. Observe preemption when shorter jobs arrive
```

#### Round Robin
```
1. Add multiple processes
2. Select "Round Robin" from dropdown
3. Set Time Quantum (e.g., enter "3" in quantum field)
4. Click "Run Scheduler"
5. Watch processes switch after each quantum
```

#### MLFQ (Multi-Level Feedback Queue)
```
1. Add processes (all start in Queue 0 — highest priority)
2. Select "MLFQ" from dropdown
3. Click "Run Scheduler"
4. Observe:
   - Short processes finish quickly in Q0 (quantum=2)
   - Longer processes get demoted to Q1 (quantum=4), then Q2 (FCFS)
   - Starving processes get promoted back up after waiting ≥10 time units
```

### Sample Data

Click the **Load Sample Data** button to automatically populate the process queue with test data:
- P1: Arrival=0, Burst=5
- P2: Arrival=1, Burst=3
- P3: Arrival=2, Burst=8
- P4: Arrival=3, Burst=6

### Understanding Results

The output displays:
- **Gantt Chart**: Visual timeline of process execution
- **Average Turnaround Time**: (Completion Time - Arrival Time) averaged across all processes
- **Average Waiting Time**: (Turnaround Time - Burst Time) averaged across all processes
- **Average Response Time**: (First CPU Time - Arrival Time) averaged across all processes
- **Individual Process Metrics**: Detailed statistics for each process

## Project Structure

```
process_algorithm/
├── src/
│   └── com/
│       └── example/
│           └── processsim/
│               ├── ProcessSimulator.java      # Main GUI application
│               ├── IconCreator.java           # Icon generation utility
│               ├── IconGenerator.java         # Icon helper
│               └── algorithms/
│                   ├── SchedulingAlgorithm.java  # Base class
│                   ├── FCFSAlgorithm.java        # FCFS implementation
│                   ├── SJFAlgorithm.java         # SJF implementation
│                   ├── SRTFAlgorithm.java        # SRTF implementation
│                   ├── RoundRobinAlgorithm.java  # RR implementation
│                   ├── MLFQAlgorithm.java        # MLFQ implementation
│                   ├── Proc.java                 # Process data structure
│                   ├── GanttEntry.java           # Gantt chart entry
│                   └── SimResult.java            # Simulation results
└── resources/
    └── icon.png                                  # Application icon
```

## Tips for Testing

1. **Test with varied arrival times**: See how each algorithm handles asynchronous arrivals
2. **Test with similar burst times**: Compare performance when processes are equal
3. **Test with one long process**: Observe convoy effect in FCFS vs other algorithms
4. **Adjust time quantum in RR**: See how it affects context switches and performance
5. **Watch MLFQ demotion**: Add a mix of short and long burst-time processes to see how they migrate across queues

## Troubleshooting

### Application won't compile
- Ensure JDK 11+ is installed: `java -version`
- Check that all source files are present in the correct package structure

### GUI doesn't appear
- Verify Java Swing is available (included in standard JDK)
- Try running with: `java -Djava.awt.headless=false -cp bin com.example.processsim.ProcessSimulator`

### Incorrect results
- Verify input values are non-negative integers
- Ensure arrival times are in ascending order for predictable behavior
- For Round Robin, set a reasonable time quantum (1-5)

## License

This project is provided as-is for educational purposes.

## Contributing

Feel free to fork, modify, and submit pull requests for improvements or additional scheduling algorithms.
