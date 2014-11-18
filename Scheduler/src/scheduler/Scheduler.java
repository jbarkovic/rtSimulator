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

		long [] periods = new long[tasks.length];
		for (int i=0;i<periods.length;i++) {
			periods[i] = tasks[i][1];
		}
		
		if (timeLimit == 0) timeLimit = (int) lcm (periods);
		String [][] message = new String[tasks.length][timeLimit];
		
		/**START**/
		
		Schedule schedule = new Schedule (tasks);
		schedule.deferrableServerHasHighestPriority = this.deferrableServerHasHighestPriority;
		schedule.defferableServerExecBuget = this.defferableServerExecBuget;
		schedule.defferableServerPeriod = this.defferableServerPeriod;
		
		int [][] scheduleArray = new int [tasks.length][timeLimit];
		for (int time=0;time<timeLimit;time++) {
			int [] timeSlice = schedule.getTimeSlice();
			for (int row=0;row<scheduleArray.length;row++) {
				scheduleArray[row][time] = timeSlice [row];
			}
		}
		for (int row=0;row<message.length;row++) {
			for (int col=0;col<message[row].length;col++) {
				switch (scheduleArray [row][col]) {
					case -1 :{
						message [row][col] = "M";
						break;
					}
					case 0  : {
						message [row][col] = " ";
						break;
					}
					case 1  : {
						message [row][col] = "#";
						break;
					}
				}
			}
		}
		/** END **/
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
		String feasibleMessage = "System " + ((schedule.feasible) ? "IS" : "IS NOT" ) + " feasible" + ((schedule.feasible) ? "" : (", Task " + (schedule.firstTaskMissDeadline+1) + " misses it's deadline of t=" + schedule.notFeasibleAt) );
		System.out.println(feasibleMessage);
		double U = getUtilization(tasks);
		System.out.println("\nUtilization = " + U);
		return schedule.feasible;
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
