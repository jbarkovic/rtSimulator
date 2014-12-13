package scheduler;

import java.util.ArrayList;

public class DeferrableServer extends PeriodicTask{
	protected boolean deferrableServerHasHighestPriority = true;
	
	private int [][] tasks;
	private final int FULL_BUDGET;
	private int budget;
	private ArrayList <Task> serverQueue = new ArrayList<Task> ();
	public DeferrableServer(int period, int budget, int [][] tasks) {
		super (0,period,-1,0,0);
		/* Set the combined execution times of async tasks as the Task "execution" number for interest */ 
		int execution = 0;
		for (int i=0;i<tasks.length;i++) {
			execution += tasks [i][0];
		}
		this.executionRemaining = execution;
		this.TOTAL_EXECUTION = execution;
		
		this.tasks = tasks;
		this.FULL_BUDGET = budget;
		this.replenishBudget();
	}
	@Override
	public boolean isDone () {
		return false;
	}
	public void replenishBudget () {
		this.budget = this.FULL_BUDGET;
	}
	@Override
	public RunResult [] step (long timeInstance) {
		RunResult [] retEvents = new RunResult [2];
		/* First Check if any tasks need to be dispatched*/
		for (int i=0;i<tasks.length;i++) {
			if (tasks[i][3] == timeInstance) {
				Task newTask = new Task (tasks[i][1],i);
				serverQueue.add(newTask);
				retEvents [0] = RunResult.TASK_DISPATCHED;
			}
		}
		/* Then run the current task (FIFO) */
		if (!serverQueue.isEmpty() && budget > 0) {
			serverQueue.get(0).step(timeInstance);
			if (serverQueue.get(0).isDone()) serverQueue.remove(0);
			budget --;
			retEvents [1] = RunResult.EXECUTED;
		} else {
			retEvents [1] = RunResult.UNABLE_TO_RUN;		
		}
		return retEvents;
	}
	@Override
	public int compareTo(Object o) {
		if (o instanceof Task && deferrableServerHasHighestPriority && budget > 0) return 1;
		if (o instanceof PeriodicTask) {
			if (o instanceof DeferrableServer) return 0;
			else {
				return compareTo((PeriodicTask) o);
			}
		} else {
			return 0;
		}
	}
}
