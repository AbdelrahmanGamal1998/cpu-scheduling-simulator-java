package cpuscheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SRTF {
    int context,lastend =0;
    ArrayList<Integer> arrivalTimes = new ArrayList<>();
    HashMap<String,Integer> BurstTimes = new HashMap<>();       //burst time for each process
    ArrayList<save> processes = new ArrayList<save>();          //output
    ArrayList<Process> pro = new ArrayList<Process>();          //input
    ArrayList<Process> ready = new ArrayList<Process>();        //ready queue
    double AWT = 0;        //average waiting time
    double ATAT = 0;       //average turn around time
    
    SRTF(Input in){
     
        context = in.timeContext;
        for(int i=0 ; i<in.numberOfProcesses ; i++)
        { 
            arrivalTimes.add(in.arrivalTimes.get(i));
            BurstTimes.put(in.names.get(i), in.burstTimes.get(i));
            pro.add( new Process(in.names.get(i),in.colors.get(i),in.arrivalTimes.get(i),in.burstTimes.get(i)));
        }
  
        Run();
    }
    
    class save{
        String name , color; 
        int arr = 0 , burst=0;
        int BT , ET;        //Begin time , End time
        double WT=0,TAT=0;  //waiting time , turn around time
        save(String a , String col, int b , int c ,int ar , int burs){name = a; color = col ;BT = b ; ET = c; arr = ar; burst=burs;}
        public void setCon(int a , int b){BT = a ;ET = b;}
    }
    class Process
    { 
        String name,color; // name 
        int bt,art; // Burst Time ,Arrival Time
        public Process(String n,String c, int a,int b ) {name = n; color = c; art = a; bt = b;}
        public double getcomp(){return  Double.parseDouble( Integer.toString(this.art) + "." + Integer.toString(this.bt) );}
    }
    public void Run(){
        int low = Collections.min(arrivalTimes);
        int high = Collections.max(arrivalTimes);
        Comparator<Process> comparator = Comparator.comparing(Process->Process.art);
        Comparator<Process> comparator2 = Comparator.comparing(Process->Process.bt);
        Comparator<save> comparator3 = Comparator.comparing(save->save.name);
       
        pro.sort(comparator);
        for(int i = low; i <= high ;++i){
            boolean flag = true;
            while(flag){
                if(pro.size()>0){
                    if(pro.get(0).art == i ){
                        ready.add(pro.get(0));
                        pro.remove(0);
                    }
                    else flag = false;
                }
                else    flag = false;
            }
            ready.sort(comparator2);
            if(ready.size()>0){
                processes.add(new save( ready.get(0).name, ready.get(0).color, i, i+1 , ready.get(0).art ,ready.get(0).bt) );
                Process neew = ready.get(0);
                neew.bt-- ;
                if(neew.bt == 0){
                    ready.remove(0);
                }
                else    ready.set(0 ,neew);
            }
        }
        ready.sort(comparator2);
        for (int i=0 ;i < ready.size(); ++i){
            processes.add(new save( ready.get(i).name, ready.get(i).color, processes.get(processes.size()-1).ET, ready.get(i).bt +processes.get(processes.size()-1).ET , ready.get(i).art , ready.get(i).bt) );
        }
        for (int i=0 ; i < processes.size()-1 ; ++i){
            if( i != processes.size()-1 ){
                while(processes.get(i).name.matches(processes.get(i+1).name)){
                    processes.set(i, new save(processes.get(i).name, processes.get(i).color, processes.get(i).BT, processes.get(i+1).ET , processes.get(i).arr ,processes.get(i).burst ));
                    processes.remove(i+1);
                    if( i == processes.size()-1 )   break;
                }
            }
        }
        for(int i=0 ; i < processes.size();++i){        //printing
            processes.get(i).setCon(processes.get(i).BT + i*context, processes.get(i).ET + i*context);
            processes.get(i).TAT = Double.parseDouble(Integer.toString(processes.get(i).ET - processes.get(i).arr)) ;
            processes.get(i).WT  = processes.get(i).TAT - BurstTimes.get(processes.get(i).name) ;
        }
        lastend =processes.get(processes.size()-1).ET;
        processes.sort(comparator3);
        for (int i=0 ; i < processes.size() ; ++i){
            double valWT = processes.get(i).WT;
            double valTAT = processes.get(i).TAT;
            if( i!=processes.size()-1){
                while(processes.get(i).name.matches(processes.get(i+1).name)){
                    valWT = processes.get(i+1).WT;
                    valTAT = processes.get(i+1).TAT;
                    i++;
                    if (i == processes.size()-1 )break;
                }
            }
            AWT+=valWT;
            ATAT+=valTAT;
        }
        AWT/=arrivalTimes.size();
        ATAT/=arrivalTimes.size();
  
    }
}
