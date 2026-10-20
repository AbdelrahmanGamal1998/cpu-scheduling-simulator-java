# CPU Scheduling Simulator (Java)

[![Build](https://github.com/AbdelrahmanGamal1998/cpu-scheduling-simulator-java/actions/workflows/build.yml/badge.svg)](https://github.com/AbdelrahmanGamal1998/cpu-scheduling-simulator-java/actions/workflows/build.yml)

A Swing application that simulates four CPU scheduling algorithms and shows the results as tables and a
colored Gantt-style chart. It was assignment 3 of my Operating Systems course (2021).

| Scheduler                                     | Class                | How it works                                                                                           |
| --------------------------------------------- | -------------------- | ------------------------------------------------------------------------------------------------------ |
| Non-preemptive Shortest Job First (SJF)       | `SJB_NonPre`         | When the CPU is free, run the arrived process with the shortest burst time                             |
| Shortest Remaining Time First (SRTF)          | `SRTF`               | Preemptive SJF with context switching: a shorter job that arrives takes the CPU                        |
| Non-preemptive Priority                       | `PriorityScheduling` | The lowest priority number runs first; waiting processes slowly gain priority so they cannot starve    |
| AG scheduling                                 | `AG`                 | A custom algorithm from the course: round robin with a changing quantum and an AG-factor (see below)   |

**Input:** number of processes, round robin time quantum, context switching time, and for each process a
name, color, arrival time, burst time and priority.
**Output:** for each scheduler, the execution order, the waiting and turnaround time of each process, their averages,
and a chart. For AG scheduling it also lists the history of every quantum update.

### AG scheduling

The AG-factor of a process is `priority + arrival time + burst time`; a smaller factor wins.

- A process first runs non-preemptively for `ceil(50%)` of its quantum. After that, a ready process with a smaller
  AG-factor can preempt it.
- If it uses its whole quantum and still has work, it goes to the end of the ready queue and its quantum grows by
  `ceil(10% of the mean quantum)`. The head of the queue runs next.
- If it is preempted early, it goes to the end of the queue and its quantum grows by the unused part. The process
  that preempted it runs next.
- When it finishes, its quantum becomes zero.

## Run it

You need JDK 17 or newer and Maven.

```bash
git clone https://github.com/AbdelrahmanGamal1998/cpu-scheduling-simulator-java.git
cd cpu-scheduling-simulator-java
mvn compile exec:java      # opens the simulator window
mvn test                   # runs the scheduler tests
```

## Tests

`src/test/java/cpuscheduler/SchedulersTest.java` checks each scheduler against a worked example:

- SJF, SRTF and Priority use a textbook example, for instance P1(0,7), P2(2,4), P3(4,1), P4(5,4) gives the order
  P1, P3, P2, P4 for SJF with an average waiting time of 4.0.
- AG uses the example from the assignment brief: the order `P1 P2 P3 P1 P3 P2 P3 P1 P2 P1 P4`, the time boundaries
  `0 3 5 9 12 17 20 21 25 26 33 37`, and every quantum update.

## Notes

- The window itself is not covered by the tests, only the scheduling logic.
- Changes made when the project was cleaned up: it is now a Maven project in package `cpuscheduler`, and the chart
  colors are loaded from the classpath (they were read from a hard-coded `D:\Education\...` folder, so the chart did
  not work on any other computer). Bugs fixed: SJF ran jobs in arrival order, not shortest first, and started jobs
  late after an idle gap; SJF and SRTF kept their results in a static list, so a second simulation added to the first
  one; and the AG scheduler did not follow the example in the assignment, so its core loop was rewritten.
- The NetBeans build files, the compiled classes and the instructor's assignment PDF are not included.

## License

[MIT](LICENSE)
