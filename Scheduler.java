import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Scheduler {
    public static void main(String[] args) throws IOException {
        List<Process> processes = new ArrayList<>();
        //rand dataset
        processes.add(new Process(1 ,1, 5, 6));
        processes.add(new Process(2 ,2, 8, 2));
        processes.add(new Process(3 ,3, 2, 8));
        processes.add(new Process( 4,4, 9, 4));
        processes.add(new Process(5,5, 3, 5));
        processes.add(new Process(6,10, 2, 3));
        processes.add(new Process(7 ,12, 1, 5));
        processes.add(new Process(8 ,15, 7, 2));
        processes.add(new Process(9 ,18, 6, 7));
        processes.add(new Process(10 ,25, 4, 4));
        List<SchedulingResult> results = new ArrayList<>();
        results = FCFS(processes);
        System.out.println(listToString(results));

        results = SJF(processes);
        System.out.println(listToString(results));

        results = roundRobin(processes, 5);
        System.out.println(listToString(results));

        results = priorityScheduling(processes);
        System.out.println(listToString(results));

        results = PBRR(processes, 5);
        System.out.println(listToString(results));

        System.out.println("<<<<<<<<<RR과 PBRR 비교를 위한 추가 데이터 셋>>>>>>>>>>");
        List<Process> plusdataset = new ArrayList<>();
        plusdataset.add(new Process(1 ,0, 22, 4));
        plusdataset.add(new Process(2 ,0, 18, 2));
        plusdataset.add(new Process(3 ,0, 9, 1));
        plusdataset.add(new Process(4 ,0, 10, 3));
        plusdataset.add(new Process(5 ,0, 4, 5));
        results = roundRobin(plusdataset, 5);
        System.out.println(listToString(results));
        results = PBRR(plusdataset, 5);
        System.out.println(listToString(results));

        try{

            BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
            String srt = in.readLine();

        }catch(Exception e) {

            System.out.println("Something went wrong.");

        }
    }

    public static String listToString(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toString());
        }
        return sb.toString();
    }


    public static List<SchedulingResult> FCFS(List<Process> processes) {
        List<SchedulingResult> results = new ArrayList<>();
        List<Integer> schedule = new ArrayList<>();

        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        ArrayList<Process> processList = new ArrayList<Process>(processes);
        Collections.sort(processList, new Comparator<Process>() {
            @Override
            public int compare(Process p1, Process p2) {
                return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            }
        });

        for (Process p : processList) {
            int arrivalTime = p.getArrivalTime();
            int burstTime = p.getBurstTime();

            if (arrivalTime > currentTime) {
                currentTime = arrivalTime;
            }

            int waitingTime = currentTime - arrivalTime;
            totalWaitingTime += waitingTime;
            int turnaroundTime = waitingTime + burstTime;
            totalTurnaroundTime += turnaroundTime;
            int responseTime = waitingTime;

            results.add(new SchedulingResult(p.getPid(), turnaroundTime, waitingTime, responseTime));
            schedule.add(p.getPid());

            currentTime += burstTime;
        }
        System.out.println("============FCFS Results============");
        System.out.println("Process order: " + schedule.toString());
        System.out.println("Execution Time :" + currentTime);
        double throughput = processList.size() / (double) currentTime;
        System.out.println("Throughput: " + String.format("%.3f", throughput));
        System.out.println("Average waiting time: " + totalWaitingTime / (double) processes.size());
        System.out.println("Average turnaround time: " + totalTurnaroundTime/(double)processes.size());

        Collections.sort(results, new ResultComparator());
        return results;
    }

    public static List<SchedulingResult> SJF(List<Process> processes) {
        List<SchedulingResult> results = new ArrayList<>();
        List<Integer> schedule = new ArrayList<>();

        int currentTime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int completedProcesses = 0;

        List<Process> remainingProcesses = new ArrayList<>(processes);

        while (!remainingProcesses.isEmpty()) {
            int minBurstTime = Integer.MAX_VALUE;
            Process nextProcess = null;

            for (Process p : remainingProcesses) {
                if (p.getArrivalTime() <= currentTime && p.getBurstTime() < minBurstTime) {
                    minBurstTime = p.getBurstTime();
                    nextProcess = p;
                }
            }

            if (nextProcess != null) {
                int waitingTime = currentTime - nextProcess.getArrivalTime();
                totalWaitingTime += waitingTime;
                int turnaroundTime = waitingTime + nextProcess.getBurstTime();
                totalTurnaroundTime += turnaroundTime;
                int responseTime = waitingTime;

                results.add(new SchedulingResult(nextProcess.getPid(), turnaroundTime, waitingTime, responseTime));
                schedule.add(nextProcess.getPid());

                currentTime += nextProcess.getBurstTime();
                remainingProcesses.remove(nextProcess);
                completedProcesses++;
            } else {
                currentTime++;
            }
        }

        System.out.println("============SJF Results============");
        System.out.println("Process order: " + schedule.toString());
        System.out.println("Execution Time :" + currentTime);
        double throughput = processes.size() / (double) currentTime;
        System.out.println("Throughput: " + String.format("%.3f", throughput));
        System.out.println("Average waiting time: " + totalWaitingTime / (double) processes.size());
        System.out.println("Average turnaround time: " + totalTurnaroundTime / (double) processes.size());

        Collections.sort(results, new ResultComparator());
        return results;
    }

    public static List<SchedulingResult> roundRobin(List<Process> processelist, int quantum) {
        List<SchedulingResult> results = new ArrayList<>();
        List<String> pidList = new ArrayList<>();
        ArrayList<Process> processes = new ArrayList<Process>(processelist);
        Collections.sort(processes, new Comparator<Process>() {
            @Override
            public int compare(Process p1, Process p2) {
                return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            }
        });

        int n = processes.size();
        int[] remainingTime = new int[n];
        int[] waitingTime = new int[n];
        int[] responseTime = new int[n];
        int[] turnaroundTime = new int[n];
        int[] arrivalTime = new int[n];
        int[] burstTime = new int[n];
        int currentTime = 0;
        int completed = 0;
        int index = 0;

        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int totalResponseTime=0;

        for (int i = 0; i < n; i++) {
            remainingTime[i] = processes.get(i).getBurstTime();
        }

        while (completed != n) {
            if (remainingTime[index] > 0) {
                pidList.add(String.valueOf(processes.get(index).getPid()));
                if (remainingTime[index] <= quantum) {
                    currentTime += remainingTime[index];
                    remainingTime[index] = 0;
                    responseTime[index] = currentTime - processes.get(index).getBurstTime() - processes.get(index).getArrivalTime();
                    waitingTime[index] = responseTime[index] + processes.get(index).getArrivalTime();
                    turnaroundTime[index] = waitingTime[index] + processes.get(index).getBurstTime();
                    completed++;
                } else {
                    currentTime += quantum;
                    remainingTime[index] -= quantum;
                }
            }
            index++;
            if (index == n) {
                index = 0;
            }
        }

        for (int i = 0; i < n; i++) {
            totalWaitingTime += waitingTime[i];
            totalTurnaroundTime += turnaroundTime[i];
            totalResponseTime+=responseTime[i];
            results.add(new SchedulingResult(processes.get(i).getPid(), turnaroundTime[i], waitingTime[i], responseTime[i]));
        }
        System.out.println("============Round Robin Results============");
        System.out.println("Process order: " + pidList.toString());
        System.out.println("Execution Time :" + currentTime);
        double throughput = n / (double) currentTime;
        System.out.println(totalResponseTime/(double)processes.size());
        System.out.println("Throughput: " + String.format("%.3f", throughput));
        System.out.println("Average waiting time: " + totalWaitingTime / (double) processes.size());
        System.out.println("Average turnaround time: " + totalTurnaroundTime / (double) processes.size());

        Collections.sort(results, new ResultComparator());
        return results;
    }

    public static ArrayList<SchedulingResult> priorityScheduling(List<Process> processes) {
        ArrayList<SchedulingResult> results = new ArrayList<>();
        List<String> pidList = new ArrayList<>();
        int n = processes.size();
        int[] waitingTime = new int[n];
        int[] responseTime = new int[n];
        int[] turnaroundTime = new int[n];
        int[] arrivalTime = new int[n];
        boolean[] completed = new boolean[n];
        int currentTime = 0;
        int completedCount = 0;

        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;

        for (int i = 0; i < n; i++) {
            arrivalTime[i] = processes.get(i).getArrivalTime();
            completed[i] = false;
        }

        while (completedCount != n) {
            int highestPriorityIndex = -1;
            int highestPriority = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (!completed[i] && arrivalTime[i] <= currentTime && processes.get(i).getPriority() < highestPriority) {
                    highestPriority = processes.get(i).getPriority();
                    highestPriorityIndex = i;
                }
            }

            if (highestPriorityIndex != -1) {
                Process currentProcess = processes.get(highestPriorityIndex);
                responseTime[highestPriorityIndex] = currentTime - arrivalTime[highestPriorityIndex];
                int burstTime = currentProcess.getBurstTime();
                currentTime += burstTime;
                turnaroundTime[highestPriorityIndex] = currentTime - arrivalTime[highestPriorityIndex];
                waitingTime[highestPriorityIndex] = turnaroundTime[highestPriorityIndex] - burstTime;
                completed[highestPriorityIndex] = true;
                completedCount++;
                totalWaitingTime += waitingTime[highestPriorityIndex];
                totalTurnaroundTime += turnaroundTime[highestPriorityIndex];
                results.add(new SchedulingResult(currentProcess.getPid(), waitingTime[highestPriorityIndex],
                        turnaroundTime[highestPriorityIndex], responseTime[highestPriorityIndex]));
                pidList.add(Integer.toString(currentProcess.getPid()));
            } else {
                currentTime++;
            }
        }
        System.out.println("============Non preemptive Priority Scheduling Results============");
        System.out.println("Process order: " + pidList.toString());
        System.out.println("Execution Time :" + currentTime);
        double throughput = n / (double) currentTime;
        System.out.println("Throughput: " + String.format("%.3f", throughput));
        System.out.println("Average waiting time: " + totalWaitingTime / (double) processes.size());
        System.out.println("Average turnaround time: " + totalTurnaroundTime / (double) processes.size());

        Collections.sort(results, new ResultComparator());
        return results;
    }

    public static ArrayList<SchedulingResult> PBRR(List<Process> processList, int timeQuantum) {
        ArrayList<SchedulingResult> results = new ArrayList<>();
        ArrayList<Process> processes = new ArrayList<Process>(processList);
        List<String> pidList = new ArrayList<>();
        Collections.sort(processes, Comparator.comparingInt(Process::getPriority));
        int n = processes.size();
        int[] remainingTime = new int[n];
        int[] waitingTime = new int[n];
        int[] responseTime = new int[n];
        int[] turnaroundTime = new int[n];

        int currenttime = 0;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int totalResponseTime=0;


        for (int i = 0; i < n; i++) {
            int pid = processes.get(i).getPid();
            pidList.add(String.valueOf(processes.get(i).getPid()));
            remainingTime[pid - 1] = processes.get(i).getBurstTime() - timeQuantum;
            processes.get(i).setRemainingTime(remainingTime[pid - 1]);
            responseTime[pid - 1] = currenttime - processes.get(i).getArrivalTime();
            waitingTime[pid - 1] = currenttime - processes.get(i).getArrivalTime();
            currenttime += Math.min(timeQuantum, processes.get(i).getBurstTime());
            turnaroundTime[pid - 1] = currenttime;
            totalWaitingTime += waitingTime[pid - 1];

            if (remainingTime[pid - 1] < 0) {
                totalTurnaroundTime += turnaroundTime[pid - 1];
                results.add(new SchedulingResult(processes.get(i).getPid(), turnaroundTime[pid - 1], waitingTime[pid - 1], responseTime[pid - 1]));
            }
        }

        Collections.sort(processes, Comparator.comparingInt(Process::getRemainingTime));

        for (int i = 0; i < n; i++) {
            int pid = processes.get(i).getPid();
            if (processes.get(i).getRemainingTime() > 0) {
                pidList.add(String.valueOf(processes.get(i).getPid()));
                waitingTime[pid - 1] = currenttime - turnaroundTime[pid - 1];
                currenttime += remainingTime[pid - 1];
                turnaroundTime[pid - 1] = currenttime;
                totalWaitingTime += waitingTime[pid - 1];
                totalTurnaroundTime += turnaroundTime[pid - 1];
                results.add(new SchedulingResult(processes.get(i).getPid(), turnaroundTime[pid - 1], waitingTime[pid - 1], responseTime[pid - 1]));

            }
        }
        System.out.println("============PBRR Scheduling Results============");
        System.out.println("Process order: " + pidList.toString());
        System.out.println("Execution Time :" + currenttime);
        double throughput = n / (double) currenttime;
        System.out.println("Throughput: " + String.format("%.3f", throughput));
        System.out.println("Average waiting time: " + totalWaitingTime / (double) n);
        System.out.println("Average turnaround time: " + totalTurnaroundTime / (double) n);

        Collections.sort(results, new ResultComparator());
        return results;
    }
}


