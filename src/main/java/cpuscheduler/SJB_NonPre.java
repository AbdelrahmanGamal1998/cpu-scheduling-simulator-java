package cpuscheduler;

import java.util.ArrayList;
import java.util.Comparator;
 
public class SJB_NonPre {
 
    ArrayList<save> processes = new ArrayList<>();  // the processes, in execution order after run()
    double averageWaitingTime = 0 , averageTurnaroundTime = 0;
 
    SJB_NonPre(Input in)
    {
        for(int i=0 ; i<in.numberOfProcesses ; i++)
        {
            processes.add(new save(in.names.get(i) , in.colors.get(i) , in.arrivalTimes.get(i) , in.burstTimes.get(i)));
        }
        run();
    }
    class save{
        String name , color;
        int arrivalTime , burstTime;
        int waitingTime , turnAroundTime;
        int beginTime , endTime;
 
        save(String n , String col, int a , int b)
        {
            name = n;
            color = col;
            arrivalTime = a;
            burstTime = b;
        }
    }
 
    /**
     * Non-preemptive shortest job first: whenever the CPU is free, run the arrived process with the
     * shortest burst time (ties go to the earliest arrival). If nothing has arrived, the CPU waits.
     */
    public void run()
    {
        ArrayList<save> pending = new ArrayList<>(processes);
        processes.clear();

        int time = 0;
        while (!pending.isEmpty())
        {
            save next = null;
            int earliestArrival = Integer.MAX_VALUE;
            for (save p : pending)
            {
                earliestArrival = Math.min(earliestArrival, p.arrivalTime);
                if (p.arrivalTime <= time
                        && (next == null
                            || p.burstTime < next.burstTime
                            || (p.burstTime == next.burstTime && p.arrivalTime < next.arrivalTime)))
                    next = p;
            }
            if (next == null)
            {
                time = earliestArrival;
                continue;
            }

            next.beginTime = time;
            next.endTime = time + next.burstTime;
            next.turnAroundTime = next.endTime - next.arrivalTime;
            next.waitingTime = next.turnAroundTime - next.burstTime;
            time = next.endTime;

            pending.remove(next);
            processes.add(next);
        }

        //Calculate Average waiting and turnaround time
        for(int i=0 ; i<processes.size() ; i++)
        {
            averageTurnaroundTime += processes.get(i).turnAroundTime;
            averageWaitingTime += processes.get(i).waitingTime;
        }
        averageTurnaroundTime /= processes.size();
        averageWaitingTime /= processes.size();
    }
}