package scheduler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

public class Scheduler {
	int [] doneAsyncs;
	static int defferableServerExecBuget = 1;
	static int defferableServerPeriod = 6;
	static boolean deferrableServerHasHighestPriority = true;
	

	
	public boolean schedule (int[][] tasks, int timeLimit) { // return true if feasible
		ArrayList<Task> deferrableServer = new ArrayList <Task> ();
		System.out.print("Schedule: ");
		for (int [] task : tasks) {
			System.out.print("{");
			for (int value : task) {
				System.out.print(" " + value + " ");
			}
			System.out.print("} ");
		}
		System.out.print("\n");
		boolean showBars = true;
		boolean feasible = true;
		int notFeasibleAt = -1;
		int firstTaskMissDeadline = 0;
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
								message[(int) queue.get(i).id][time-1] = "M";
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
//					if (current.period <= 0 ) {
//						if (Task.AsyncExecRemain > 0) current.step(executing);
//						else {
//							passOnPriorityToNext = true;
//							current.step(false);
//						}
//					} else {
						current.step(executing);
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
				queue.clear();				
				queue.addAll(newList);
				for (int i=0;i<tasks.length;i++) {
					if (doneAsyncs[i] > 0) continue;
					//if (tasks[i][1] < 0 && AsyncTotalRemain == 0) continue; 
					if (tasks[i].length > 3) {
						Task newTask = null;
						if (tasks[i][3] == time) { // Delayed start
							if (tasks[i][1] > 0) newTask = (this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2],0,i));
							//else if (tasks[i][1] <= 0 || tasks[i][2] <= 0) queue.add(this.requestTaskInstance(tasks[i][0], 0, 0, 0,0,i)); // Deferrable Server									
							else newTask = (this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2] - time%tasks[i][2],0,i));
						} else if (tasks[i][1] != 0 && (time-tasks[i][3])%Math.abs(tasks[i][1]) == 0) { // Delayed start periodic Task
							if (tasks[i][1] > 0) newTask = (this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2],0,i));
						}
						if (newTask != null) {
							queue.add(newTask);
							deferrableServer.add(newTask);
						}
					} else if (tasks[i][1] <= 0 ) {
						if (tasks[i][1] == 0 || time % Math.abs(tasks[i][1]) == 0) {
							if (Task.AsyncTotalRemain > 0) queue.add(this.requestTaskInstance(tasks[i][0], 0, tasks[i][2], time + tasks[i][2] - time%tasks[i][2],0,i));
						}
					}  else if (tasks[i].length == 3 && time % Math.abs(tasks[i][1]) == 0) { // Periodic Task reached period here
						if (tasks[i][1] > 0) queue.add(this.requestTaskInstance(tasks[i][0], tasks[i][1], tasks[i][2], time + tasks[i][2],0,i));
					}
				}
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
		System.out.print("\n");
		for (int row = 0;row<message.length;row++) {
			if (tasks[row][1] <= 0) {
				System.out.print("DS: ");
			}
			System.out.print("(");
			for (int i=0;i<tasks[row].length;i++) {
				System.out.print(tasks[row][i]);
				if ((i+1) < tasks[row].length) System.out.print(",");				
			}
			System.out.print(") : \n");
			for (int i=0;i<=timeLimit;i++) {
				if (tasks[row][1] <= 0) {
					if (tasks[row].length > 3 && i == tasks[row][3]) {
						System.out.print("R" + ((showBars)? " " : ""));
					} else if (i % Math.abs(tasks[row][1]) == 0) {						
						System.out.print("V" + ((showBars)? " " : ""));					
					} else {					
						System.out.print(" "+((showBars)? " " : ""));
					}
				} else if (tasks[row][1] > 0 ){
					if (tasks[row].length > 3 && i == tasks[row][3]) {
						System.out.print("R" + ((showBars)? " " : ""));
					} else if (tasks[row].length > 3 && (i-tasks[row][3]) % tasks[row][1] == 0) {				
						System.out.print("V" + ((showBars)? " " : ""));
					} else if (tasks[row].length == 3 && i % Math.abs(tasks[row][1]) == 0) {
						System.out.print("V"+((showBars)? " " : ""));
					} else {
						System.out.print(" "+((showBars)? " " : ""));
					}
				}
			}
			System.out.print("\n");
			for (String element : message[row]) {
				String symbol = (element == null) ? " " : element;
				System.out.print(((showBars)? "|" : "")+symbol);
			}
			System.out.print("|\n");

		}
		int depth = 0;
		int time = timeLimit;
		while (time > 0) {
			time = time / 10;
			depth ++;
		}
		String [][] timeStamps = new String [depth][timeLimit+1];
		//String [][] timeStamps = new String [((timeLimit+1)/100)+1][timeLimit+1];
		timeStamps[0][0] = "0" + ((showBars)? " " : "");
		for (int i=0;i<timeStamps.length;i++) {
			boolean nonZeroFound = false;			
			for (int j=0;j<timeStamps[0].length;j++) {
				if ((j+i) == 0) continue;
				int val = j;
				val = (val%((int) Math.pow(10, (i+1))));			
				val = (val/((int) Math.pow(10, (i))));
				nonZeroFound = nonZeroFound || (val != 0);
				if (nonZeroFound) timeStamps[i][j] = "" + val + ((showBars)? " " : "");
				else timeStamps[i][j] = " " + ((showBars)? " " : "");
			}
			int k = i;		
		}
		for (String [] row : timeStamps) {
			for (String element : row) {
				System.out.print(element);
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		System.out.print("\n");
		String feasibleMessage = "System " + ((feasible) ? "IS" : "IS NOT" ) + " feasible" + ((feasible) ? "" : (", Task " + (firstTaskMissDeadline+1) + " misses it's deadline of t=" + notFeasibleAt) );
		System.out.println(feasibleMessage);
		double U = getUtilization(tasks);
		System.out.println("\nUtilization = " + U);
		return feasible;
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
	private Task requestTaskInstance (int exec, int period, int deadline, int absoluteDeadline,int releaseOffset, long id) {
		//System.out.println("Requested Task: exec: " + exec + " , period: " + period + " , deadline " + deadline);
		//if (period == 0 || period < 0) return AsyncTask.getInstance (exec, -period, deadline, absoluteDeadline,releaseOffset, id);
		 return getTaskInstance (exec, period, deadline, absoluteDeadline, releaseOffset, id);
	}
	protected Task getTaskInstance (int exec, int period, int deadline, int absoluteDeadline,int releaseOffset, long id) {
		return new Task (exec, period, deadline, absoluteDeadline,releaseOffset, id);
	}
}
