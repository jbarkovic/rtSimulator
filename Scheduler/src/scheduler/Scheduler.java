package scheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;

public class Scheduler {
	protected RunResult [][][] schedule;
	protected DeferrableServer [] dsList;
	protected int [][] tasks;
	protected int timeLimit;
	protected long time = 0;
	protected Schedule queue;
	
	protected static int DEBUG = 1;
	
	public Scheduler (int [][] tasks, int timeLimit, DeferrableServer [] dsList, Schedule schedAlgorithm) {
		this.queue = schedAlgorithm;
		if (dsList != null) this.dsList = dsList;
		else this.dsList = new DeferrableServer [0];
		this.tasks = tasks;
		this.timeLimit = timeLimit + 1;
	}
	protected Task getTask (int [] taskParameters, int index) {
		if (DEBUG > 0) System.out.println("Adding task number: " + index + " at time = " + time + " with parameters: " + Arrays.toString(taskParameters));		
		return new PeriodicTask (taskParameters[0], taskParameters[1], taskParameters[2], (int) time, index);
	}
	protected void dispatchTasks () {
		for (int i=0;i<tasks.length;i++) {
			if (tasks [i][1] > 0) { //Periodic
				if (tasks [i].length == 3) {
					if (time % tasks [i][1] == 0) {
						queue.add(getTask (tasks [i] , i));
					}
				} else if (tasks [i].length > 3) { /*Start With a Delay*/
					if (tasks[i][3] == time) {
						queue.add(getTask (tasks [i] , i));
					} else if (time > tasks [i][3] && (time - tasks[i][3]) % tasks[i][1] == 0) {
						queue.add(getTask (tasks [i] , i));
					}					
				}
			}
		}
		if (time == 0) {
			for (int j=0;j<this.dsList.length;j++) {
				queue.add(this.dsList[j]);
				this.dsList [j].setIndex(tasks.length + j);
			}
		}
	}
	protected RunResult [][] iterate () {
		dispatchTasks ();		
		Collections.sort(queue,queue);
		
		RunResult [][] iterationResult = new RunResult [tasks.length + dsList.length][];
		
		/* Next: Figure out if anybody has missed their deadlines*/
		ListIterator<Task> li = queue.listIterator(); // Start from the beginning in case the current task also missed its deadline
		while (li.hasNext()) {
			Task t = li.next();
			if (t instanceof PeriodicTask && !(t instanceof DeferrableServer)) {
				PeriodicTask pt = (PeriodicTask) t;
				int timeOffset = (int) time;
				/* Check For Missed Deadline */
				if (!pt.isDone() && timeOffset >= pt.getDeadline() && timeOffset == pt.getAbsoluteDeadline()) {
					if (DEBUG > 0) System.out.println("\tTask " + pt.getIndex() + " " + Arrays.toString(tasks[pt.getIndex()]) + " Missed its deadline at time t=" +time);
					
					RunResult [] thisResult = new RunResult [] {RunResult.NOT_EXECUTED,RunResult.TASK_MISSED_DEADLINE};
					iterationResult [pt.getIndex()] = thisResult;
				}
			}
		}
		for (int i=0;i<iterationResult.length;i++) {
			if (iterationResult [i] == null) {
				iterationResult [i] = new RunResult [] {RunResult.NOT_EXECUTED};
			}
		}
		li = queue.listIterator();
		FindSomethingToExecute : {
			while (li.hasNext()) {
				Task current = li.next();
				RunResult [] tempResult = current.step(time);
				if (tempResult.length == 0) {
					System.err.println("ERROR: No result from simulated task execution of task # " + current.getIndex() + ". Nothing may have been executed at time (" + time + "), please check the schedule.");
					break;
				}
				else {
					for (RunResult res : tempResult) {
						if (res == RunResult.EXECUTED) {						
							RunResult [] old = iterationResult [current.index]; 
							iterationResult [current.index] = new RunResult [old.length + tempResult.length];
							int pointer = 0;
							for (;pointer<old.length;pointer++) iterationResult [current.index][pointer] = old[pointer];
							for (int tempPointer=0;pointer<old.length+tempResult.length;tempPointer++,pointer++) iterationResult [current.index][pointer] = tempResult [tempPointer];							 
							if (current.isDone()) li.remove();
							break FindSomethingToExecute;
						}
					}
				}
			}
		}
		return iterationResult;
	} 
	public RunResult [][][] schedule () { // return true if feasible
		this.schedule = new RunResult [this.timeLimit][][];
		for (int i=0;i<this.timeLimit;i++) {
			this.schedule[i] = this.iterate();
			time++;
		}
		return this.schedule;	
	}
	
	protected double getUtilization (int [][] tasks) {
		double U = 0;
		for (int [] task : tasks) {
			if (task[1] > 0) U += (task[0]/(double)task[2]);
		}
		return U;
	}
	private static long gcd(long a, long b)
	{
	    while (b > 0)
	    {
	        long temp = b;
	        b = a % b; // % is remainder
	        a = temp;
	    }
	    return a;
	}

	private static long gcd(long[] input)
	{
	    long result = input[0];
	    for(int i = 1; i < input.length; i++) result = gcd(result, input[i]);
	    return result;
	}
	private static long lcm(long a, long b)
	{
		if (a <= 0 && b > 0) return b;
		else if (b<=0 && a > 0) return a;
		else if (a <= 0 && b <= 0) return 0;
		
	    return a * (b / gcd(a, b));
	}

	private static long lcm(long[] input)
	{
	    long result = input[0];
	    for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
	    return result;
	}
}
