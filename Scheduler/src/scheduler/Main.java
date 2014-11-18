package scheduler;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 2) {
			
		}
		Scheduler scheduler = new RM();
		int timeLimit = 0;
		int [][] lab6_p4 = new int[][] {{1,4,4},{1,8,8},{1,12,12}};
		int [][] rm_full_util = new int[][] {{3,4,4},{2,8,8}};
		int [][] pract_q5a = new int[][] {{2,5,5},{1,4,4},{2,8,8}};
		int [][] tasks_q2 = new int[][] {{1,3,3},{2,5,5},{3,8,8}};
		int [][] tasks_q3 = new int[][] {{1,10,10,7},{7,15,15},{9,25,25}};
		int [][] tasks_q4 = new int[][] {{1,6,6},{2,8,8},{6,15,15}};
		int [][] tasks_a4_q1 = new int[][] {{2,7,7},{3,10,10},{1,-6,6,2},{2,-6,6,5}};
		int [][] tasks_a4_q1i = new int[][] {{3,5,7},{1,4,5},{1,-6,6,2},{2,-6,6,5}};
		
		
		int [][] practice_2 = new int [][] {{10,50,50},{20,60,60},{17,-6,-40,35},{5,-6,-40,90}};
		scheduler.defferableServerExecBuget = 10;
		scheduler.defferableServerPeriod    = 40;
		scheduler.schedule (practice_2, timeLimit);
			
		//}
		}
}


