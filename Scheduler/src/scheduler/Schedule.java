package scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

public class Schedule {
	int [] doneAsyncs;
	static int defferableServerExecBuget = 1;
	static int defferableServerPeriod = 6;
	static boolean deferrableServerHasHighestPriority = true;
	
	boolean feasible = true;
	int notFeasibleAt = -1;
	int firstTaskMissDeadline = 0;
	
	ArrayList<Task> queue = new ArrayList<Task> ();
	ArrayList<Task> deferrableServer = new ArrayList <Task> ();
	
	int [][] tasks;
	long time = -1;
	
	public Schedule (int [][] tasks) {
		this.tasks = tasks;
		//queueTasks();
		for (int i=0;i<tasks.length;i++) {
			if (tasks[i].length == 3) {
				queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], tasks[i][1],0,i));
			} else if (tasks.length > 3) {
				if (tasks[i][3] == 0) queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], tasks[i][1],0,i));
			}
		}
	}
	protected int [] getTimeSlice () {
		int [] out = new int [tasks.length];
		time++;
		/** Begin **/
		System.out.print("QueueSize: " + queue.size() + " Queue {");
		ListIterator<Task> lr = queue.listIterator();
		while (lr.hasNext()) {
			System.out.print(lr.next().id + " , ");
		}
		System.out.println("\n");
		Task.AsyncExecRemain = defferableServerExecBuget;

		doneAsyncs = new int[tasks.length];					
				ListIterator<Task> li = queue.listIterator();
				ArrayList<Task> newList = new ArrayList<Task> ();
				if (queue.size() > 0) {
					out [(int)queue.get(0).id] = 1;
					for (int i=1;i<queue.size();i++) {
						if ((int)queue.get(i).absoluteDeadline < time && (int)queue.get(i).absoluteDeadline > 0) {
								out [(int) queue.get(i).id] = -1;
								feasible = false;
								notFeasibleAt = (int) ((notFeasibleAt==-1) ? (time-1) : notFeasibleAt);
								firstTaskMissDeadline = (int) queue.get(i).id;
							}
						}
				}
				
				while (li.hasNext()) {
					Task current = li.next();
					boolean executing = (queue.indexOf(current) == 0);// || passOnPriorityToNext;
				//	passOnPriorityToNext = false;
//					if (current.period <= 0 ) {
//						if (Task.AsyncExecRemain > 0) current.step(executing);
//						else {
//							passOnPriorityToNext = true;
//							current.step(false);
//						}
//					} else {
						current.step(executing);
						if (executing) System.out.println ("\tTask: " + (current.id) + " running at time: " + time);

					//}
					if (!current.done) {
						if (current.period <= 0 ) {
							if (Task.AsyncExecRemain > 0) newList.add(current);						
						} else {
							newList.add(current);
						}
					} else {
						System.out.println ("\tTask: " + (current.id) + " done @ time: " + time);
					}
					if (current.SIGQUIT) {
						doneAsyncs[(int)current.id] = 1;
					}
				}
//				if (li.hasNext()) {
//					Task currentExecutingTask = li.next();
//					System.out.println ("\tTask: " + (currentExecutingTask.id) + " running at time: " + time);
//					currentExecutingTask.step(true);
//					if (!currentExecutingTask.done) {
//						if (currentExecutingTask.isPeriodic()) {
//							newList.add(currentExecutingTask);
//						} else {
//							if (Task.AsyncExecRemain > 0) newList.add(currentExecutingTask);						
//						}												
//					} else {
//						System.out.println ("\tTask: " + (currentExecutingTask.id) + " done @ time: " + time);
//					}
//					if (currentExecutingTask.SIGQUIT) {
//						doneAsyncs[(int)currentExecutingTask.id] = 1;
//					}
//				}
//				while (li.hasNext()) {
//					Task current = li.next();
//					current.step(false);
//					if (!current.done) newList.add(current);
//
//				}
				queue.clear();
				printQueue("After Clear: ");
				queue.addAll(newList);
				printQueue("After newList: ");

				queueTasks ();
				manageDeferableServer ();

				ArrayList<Task> removeList = new ArrayList<Task> ();
				if (Task.AsyncExecRemain <= 0) {
					System.out.println("ASYNC TIME EXPIRED, REMOVING AT TIME(t) = " + time + " Async Buget = " + Task.AsyncExecRemain);
					for (Task t : queue) {						
						if (!t.isPeriodic()) {
							System.out.println("\tREMOVING TASK WITH EXEC: " + t.exec);							
							removeList.add(t);
						}
					}
				}
				queue.removeAll(removeList);
				Collections.sort(queue);					
		
		/** End **/				
		return out;
	}

	private void printQueue (String msg) {
		System.out.print(msg + "Queue: {");
		ListIterator<Task> lr = queue.listIterator();
		while (lr.hasNext()) {
			System.out.print(lr.next().id + " , ");
		}
		System.out.println("\n");
	}
	private void manageDeferableServer () {
		if (time % Math.abs(defferableServerPeriod) == 0) {
			if (Task.AsyncExecRemain <= 0) {
				ListIterator<Task> dsLi = deferrableServer.listIterator();
				while (dsLi.hasNext()) {
					Task task = dsLi.next();
					if (!task.SIGQUIT) {
						task.restart();
						queue.add(task);
					}
			
				}
			}
			Task.AsyncExecRemain = defferableServerExecBuget;					
		}
	}
	private void queueTasks () {
		for (int i=0;i<tasks.length;i++) {
			if (doneAsyncs[i] > 0) continue;
			if (tasks[i].length > 3) { // Task with a delayed start
				Task newTask = null;
				if (tasks[i][3] == time) { // Delayed start time is now
					if (tasks[i][1] > 0) newTask = (this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], (int) (time + tasks[i][2]),0,i)); // Has a deadline
					//else if (tasks[i][1] <= 0 || tasks[i][2] <= 0) queue.add(this.requestTaskInstance(tasks[i][0], 0, 0, 0,0,i)); // Deferrable Server									
					else newTask = (this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], (int)(time + tasks[i][2] - time%tasks[i][2]),0,i));
				} else if (tasks[i][1] != 0 && (time-tasks[i][3])%Math.abs(tasks[i][1]) == 0) { // Delayed start periodic Task
					if (tasks[i][1] > 0) newTask = (this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], (int)(time + tasks[i][2]),0,i));
				}
				if (newTask != null) {
					queue.add(newTask);
					deferrableServer.add(newTask);
				}
			} else if (tasks[i][1] <= 0 ) {
				if (tasks[i][1] == 0 || time % Math.abs(tasks[i][1]) == 0) {
					if (Task.AsyncTotalRemain > 0) queue.add(this.requestTaskInstance(tasks[i][0], 0, tasks[i][2], (int)(time + tasks[i][2] - time%tasks[i][2]),0,i));
				}
			}  else if (tasks[i].length == 3 && time % Math.abs(tasks[i][1]) == 0) { // Periodic Task reached period here
				if (tasks[i][1] > 0) queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], (int)(time + tasks[i][2]),0,i));
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
