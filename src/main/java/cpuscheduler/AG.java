package cpuscheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * AG scheduling, as described in the assignment.
 *
 * <ul>
 * <li>AG-factor = priority + arrival time + burst time (a smaller factor wins).</li>
 * <li>A process that gets the CPU first runs non-preemptively for ceil(50%) of its quantum, then it can be
 * preempted by a ready process with a smaller AG-factor.</li>
 * <li>Used the whole quantum and still has work: it goes to the end of the ready queue and its quantum grows by
 * ceil(10% of the mean quantum of all processes). The next process is the head of the queue.</li>
 * <li>Preempted before using the whole quantum: it goes to the end of the queue and its quantum grows by the unused
 * part. The process that preempted it runs next.</li>
 * <li>Finished: its quantum becomes zero. The next process is the head of the queue.</li>
 * </ul>
 */
public class AG {
    int arrivalTime;
    String name;
    int quantumTime;
    int burstTime;
    int priority;
    int AGFactor = 0;
    String Color = "NON";

    /** One turn on the CPU: from AT (start) to BT (end). */
    class save {
        String name, color;
        int AT, BT;         // start time, end time
        int WT, TAT;        // filled in on the last turn of each process
    }

    ArrayList<save> outPut = new ArrayList<save>();
    /** Every quantum update, in order: the initial quantums first, then one entry per change. */
    ArrayList<AG> quantumHistory = new ArrayList<AG>();

    private final ArrayList<AG> processes = new ArrayList<AG>();
    private final ArrayList<AG> original = new ArrayList<AG>(); // arrival and burst time before the run

    AG() {}

    AG(Input input) {
        for (int i = 0; i < input.numberOfProcesses; i++) {
            AG p = new AG();
            p.name = input.names.get(i);
            p.Color = input.colors.get(i);
            p.arrivalTime = input.arrivalTimes.get(i);
            p.burstTime = input.burstTimes.get(i);
            p.priority = input.priorityNumbers.get(i);
            p.quantumTime = input.timeQuantum;
            p.AGFactor = p.burstTime + p.arrivalTime + p.priority;
            processes.add(p);

            AG copy = new AG();
            copy.name = p.name;
            copy.arrivalTime = p.arrivalTime;
            copy.burstTime = p.burstTime;
            original.add(copy);
        }
        schedule();
    }

    private void record(AG p) {
        AG entry = new AG();
        entry.name = p.name;
        entry.quantumTime = p.quantumTime;
        quantumHistory.add(entry);
    }

    private void saveTurn(AG p, int start, int end) {
        save turn = new save();
        turn.name = p.name;
        turn.color = p.Color;
        turn.AT = start;
        turn.BT = end;
        outPut.add(turn);
    }

    private int meanQuantum10Percent() {
        double sum = 0;
        for (AG p : processes) sum += p.quantumTime;
        return (int) Math.ceil(sum / processes.size() * 0.10);
    }

    private void schedule() {
        List<AG> arrivalOrder = new ArrayList<AG>(processes);
        arrivalOrder.sort((a, b) -> Integer.compare(a.arrivalTime, b.arrivalTime)); // stable: ties keep input order
        for (AG p : processes) record(p);

        List<AG> ready = new ArrayList<AG>(); // FIFO queue of arrived processes that are not running
        int admitted = 0;
        int time = 0;
        int alive = processes.size();
        AG running = null;

        while (alive > 0) {
            while (admitted < arrivalOrder.size() && arrivalOrder.get(admitted).arrivalTime <= time) {
                ready.add(arrivalOrder.get(admitted++));
            }

            if (running == null) {
                if (ready.isEmpty()) { // the CPU is idle until the next process arrives
                    time = arrivalOrder.get(admitted).arrivalTime;
                    continue;
                }
                running = ready.remove(0);
            }

            int start = time;
            int worked = 0;
            AG next = null; // the process that preempts the running one, if any

            // First half of the quantum: non-preemptive.
            int half = (int) Math.ceil(running.quantumTime / 2.0);
            int slice = Math.min(half, running.burstTime);
            time += slice;
            worked += slice;
            running.burstTime -= slice;

            // Rest of the quantum: preemptive, one time unit at a time.
            while (running.burstTime > 0 && worked < running.quantumTime) {
                while (admitted < arrivalOrder.size() && arrivalOrder.get(admitted).arrivalTime <= time) {
                    ready.add(arrivalOrder.get(admitted++));
                }
                AG best = null;
                for (AG p : ready) if (best == null || p.AGFactor < best.AGFactor) best = p;
                if (best != null && best.AGFactor < running.AGFactor) {
                    next = best;
                    break;
                }
                time++;
                worked++;
                running.burstTime--;
            }

            saveTurn(running, start, time);
            while (admitted < arrivalOrder.size() && arrivalOrder.get(admitted).arrivalTime <= time) {
                ready.add(arrivalOrder.get(admitted++));
            }

            if (running.burstTime == 0) { // finished
                running.quantumTime = 0;
                record(running);
                alive--;
                running = ready.isEmpty() ? null : ready.remove(0);
            } else if (next != null) { // preempted: it gets its unused quantum back, the preemptor runs next
                running.quantumTime += running.quantumTime - worked;
                record(running);
                ready.add(running);
                ready.remove(next);
                running = next;
            } else { // used the whole quantum: it grows by 10% of the mean quantum
                running.quantumTime += meanQuantum10Percent();
                record(running);
                ready.add(running);
                running = ready.remove(0);
            }
        }

        fillWaitingAndTurnaround();
    }

    private void fillWaitingAndTurnaround() {
        for (AG p : original) {
            save last = null;
            for (save s : outPut) if (s.name.equals(p.name)) last = s;
            last.TAT = last.BT - p.arrivalTime;
            last.WT = last.TAT - p.burstTime;
        }
    }

    public double getAwt() {
        double total = 0;
        for (save s : outPut) total += s.WT;
        return total / original.size();
    }

    public double getATAT() {
        double total = 0;
        for (save s : outPut) total += s.TAT;
        return total / original.size();
    }

    public void getQuantumHistory() {
        for (AG entry : quantumHistory) {
            System.out.println(entry.name + " " + entry.quantumTime);
        }
    }
}
