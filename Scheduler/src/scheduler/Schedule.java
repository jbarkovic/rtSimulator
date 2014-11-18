package scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

public class Schedule {
	protected Task [] tasks;
	protected long currentTime = -1;
	ArrayList<Schedule> queue = new ArrayList<Schedule> ();
	
	
	public Schedule (int [][] tasks) {
		for (int i=0;i<tasks.length;i++) {
			int [] task = tasks [i];
			this.tasks[i] = getTaskInstance (task, i);
		}
	}
	
	public int [] getTimeSlice () { // return true if feasible
		ArrayList<Task> queue = new ArrayList<Task> ();
		if (tasks.length == 0) return true;
		long [] periods = new long[tasks.length];
		for (int i=0;i<periods.length;i++) {
			periods[i] = tasks[i][1];
		}
		Task.AsyncExecRemain = defferableServerExecBuget;
		if (timeLimit == 0) timeLimit = (int) lcm (periods);
		doneAsyncs = new int[tasks.length];
		for (int i=0;i<tasks.length;i++) {
			if (tasks[i].length == 3) {
				queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], tasks[i][1],0,i));
			} else if (tasks.length > 3) {
				if (tasks[i][3] == 0) queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], tasks[i][1],0,i));
			}
		}		
		String [][] message = new String[tasks.length][timeLimit];
		if (queue.size() > 0) {
			for (int time=1;time<timeLimit+1;time++) {				
				ListIterator<Task> li = queue.listIterator();
				ArrayList<Task> newList = new ArrayList<Task> ();
				if (queue.size() > 0) {
						message[(int)queue.get(0).id][time-1] = "#";
						for (int i=1;i<queue.size();i++) {
							if ((int)queue.get(i).absoluteDeadline < time && (int)queue.get(i).absoluteDeadline > 0) {
								message[(int)queue.get(i).id][time-1] = "M";
								feasible = false;
								notFeasibleAt = (notFeasibleAt==-1) ? (time-1) : notFeasibleAt;
								firstTaskMissDeadline = (int) queue.get(i).id;
							}
						}
				}
				for (String [] elements : message) {
					if (elements[time-1] == null) elements[time-1] = " ";
				}
				boolean passOnPriorityToNext = false;
				while (li.hasNext()) {
					Task current = li.next();
					boolean executing = (queue.indexOf(current) == 0);// || passOnPriorityToNext;
					passOnPriorityToNext = false;
					if (!current.done) {
						if (current.period <= 0 ) {
							if (Task.AsyncExecRemain > 0) newList.add(current); 						
						} else {
							newList.add(current);
						}
					} else {
						System.out.println ("\tTask: " + (current.id+1) + " done @ time: " + time);
					}
					if (current.SIGQUIT) {
						doneAsyncs[(int)current.id] = 1;
					}
				}
				queue.clear();				
				queue.addAll(newList);
				for (int i=0;i<tasks.length;i++) {
					if (doneAsyncs[i] > 0) continue;
					//if (tasks[i][1] < 0 && AsyncTotalRemain == 0) continue; 
					if (tasks[i].length > 3 && tasks[i][3] == time) { // Delayed start
						if (tasks[i][1] > 0) queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2],0,i));
						else queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2] - time%tasks[i][2],0,i));
					} else if (tasks[i][1] <= 0 && time % Math.abs(tasks[i][1]) == 0) {
						if (Task.AsyncTotalRemain > 0) queue.add(this.requestTaskInstance(tasks[i][0], 0, tasks[i][2], time + tasks[i][2] - time%tasks[i][2],0,i));
					} else if (tasks[i].length > 3 && (time-tasks[i][3])%Math.abs(tasks[i][1]) == 0) { // Delayed start period
						if (tasks[i][1] > 0) queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2],0,i));
					} else if (tasks[i].length == 3 && time % Math.abs(tasks[i][1]) == 0) { // at period
						if (tasks[i][1] > 0) queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2],0,i));
					}
				}
				if (time % Math.abs(defferableServerPeriod) == 0) Task.AsyncExecRemain = defferableServerExecBuget;
				ArrayList<Task> removeList = new ArrayList<Task> ();
				if (Task.AsyncExecRemain <= 0) {
					System.out.println("ASYNC TIME EXPIRED, REMOVING AT TIME(t) = " + time + " Async Buget = " + Task.AsyncExecRemain);
					for (Task t : queue) {						
						if (t.weAreAsync) {
							System.out.println("\tREMOVING TASK WITH EXEC: " + t.exec);
							removeList.add(t);
						}
					}
				}
				queue.removeAll(removeList);
				Collections.sort(queue);
			}		

		}

	}
	protected double getUtilization (int [][] tasks) {
		double U = 0;
		for (int [] task : tasks) {
			if (task[1] > 0) U += (task[0]/(double)task[2]);
		}
		return U;
	}
	private Task requestTaskInstance (int exec, int period, int deadline, int absoluteDeadline,int releaseOffset, long id) {
		//System.out.println("Requested Task: exec: " + exec + " , period: " + period + " , deadline " + deadline);
		//if (period == 0 || period < 0) return AsyncTask.getInstance (exec, -period, deadline, absoluteDeadline,releaseOffset, id);
		 return getTaskInstance (exec, period, deadline, absoluteDeadline, releaseOffset, id);
	}
	protected Task getTaskInstance (int exec, int period, int deadline, int absoluteDeadline,int releaseOffset, long id) {
		return new Task (exec, period, deadline, absoluteDeadline,releaseOffset, id);
	}
	
}
