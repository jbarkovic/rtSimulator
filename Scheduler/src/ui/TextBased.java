package ui;

import scheduler.*;

public class TextBased {
	private final static int LABEL_LENGTH = 20; 
	public static void main(String[] args) {
		if (args.length > 2) {
			
		}
		Scheduler scheduler;
		int timeLimit = 0;
		int [][] lab6_p4 = new int[][] {{1,4,4},{1,8,8},{1,12,12}};
		int [][] rm_full_util = new int[][] {{3,4,4},{2,8,8}};
		
		//int [][] pract_q5a = new int[][] {{2,5,5,3},{1,4,4},{2,8,8}};
		int [][] pract_q5a = new int[][] {{2,6,6},{3,8,8},{2,12,12}};
		int [][] tasks_q2 = new int[][] {{1,3,3},{2,5,5},{3,8,8}};
		int [][] tasks_q3 = new int[][] {{1,10,10,7},{7,15,15},{9,25,25}};
		int [][] tasks_q4 = new int[][] {{1,6,6},{2,8,8},{6,15,15}};
		int [][] tasks_a4_q1 = new int[][] {{2,7,7},{3,10,10},{1,-6,6,2},{2,-6,6,5}};
		int [][] tasks_a4_q1i = new int[][] {{3,5,7},{1,4,5},{1,-6,6,2},{2,-6,6,5}};
		int [][] practice_2 = new int [][] {{10,50,50},{20,60,60},{17,-6,-40,35},{5,-6,-40,90}};
		
		final int [][] task_list = pract_q5a; 
		scheduler = new Scheduler (task_list, 24, null, new EDF ());
		RunResult [][][] schedule = scheduler.schedule();
		
		printSchedule (schedule, task_list);
	}
	protected static void printSchedule (RunResult [][][] schedule, int [][] task_list) { 
		String [][] display = new String [schedule[0].length*2][schedule.length]; 
		for (int time_slice=0;time_slice<schedule.length;time_slice++) {
			for (int task=0;task<(display.length/2);task++) {
				boolean isExecution = false;
				boolean deadlineMissedHere = false; // True if this task misses a deadline at this time instance
				for (RunResult res : schedule[time_slice][task]) {
					switch (res) {
					case EXECUTED 				: {display [task*2 + 1][time_slice] = "#"; break;}
					case TASK_MISSED_DEADLINE 	: {deadlineMissedHere = true; break;}//{display [task*2 + 1][time_slice] = "M"; break;}
					case NOT_EXECUTED 			: {display [task*2 + 1][time_slice] = " "; break;}
					default : if (display [task*2 + 1][time_slice] == null) display [task*2 + 1][time_slice] = "X";
					}
				//	if (res == RunResult.TASK_MISSED_DEADLINE) break; // Missed Deadlines are more important
				}
				int [] currentTask = task_list [task];
				if (deadlineMissedHere) display [task*2][time_slice] = "M";
				else if (currentTask.length == 3 && time_slice % currentTask [1] == 0) display [task*2][time_slice] = "V";
				else if (currentTask.length >  3 && (time_slice - currentTask[3]) % currentTask [1] == 0) display [task*2][time_slice] = "V";
				else display [task*2][time_slice] = " "; // will be indicators of release time, deadline, etc..
			}
		}
		System.out.println("\n\n\n");
		for (int i=0;i<(display [0].length*2 + LABEL_LENGTH + 5); i++) {
			System.out.print("=");
		}
		System.out.println("\n\t\t  Schedule");
		for (int i=0;i<(display [0].length*2 + LABEL_LENGTH + 5); i++) {
			System.out.print("=");
		}
		System.out.println();
		char [] lineLabelBuffer = new char [LABEL_LENGTH];
		for (int line = 0;line < display.length;line++) {
			String label = String.format("Task[%d] {", (line/2));
			
			//System.out.print ("Task[" + (line/2) + "] {");
			for (int i=0;i<task_list[line/2].length;i++) {
				label += (task_list[line/2][i] + ((i == task_list[line/2].length-1) ? "" : ","));
			}
			label += "}";
			String out_buffer = String.format("%-" + LABEL_LENGTH + "s\t", "");
			if (line % 2 == 0) out_buffer = String.format("%-" + LABEL_LENGTH + "s\t", label);
			System.out.print(out_buffer);
			//System.out.print("}\t\t");
			for (int chr=0;chr<display[0].length;chr++) {
				if (line % 2 == 0) { // the indicator lines
					String leftFiller  = "";
					String rightFiller = " "; 
					System.out.print(leftFiller + display[line][chr] + rightFiller);
				} else {
					String leftFiller  = ((line % 2 == 0) ? " " : "|");
					String rightFiller = ""; 
					System.out.print(leftFiller + display[line][chr] + rightFiller);
				}
			}
			System.out.println((line % 2 == 0) ? " " : "|");
		}
		
		System.out.print(String.format("%-" + LABEL_LENGTH + "s\t", "TIME_SLICE:"));
		for (int sig_dig = 10; sig_dig < display [0].length * 10; sig_dig *= 10) {
			
			for (int i=0;i<display[0].length + 1;i++) { // Is plus one in order to match the input bound (ie draw a 50 instead of stopping at 49)
				int dig_out = i % sig_dig;
				do {
					if (sig_dig > 10) dig_out /= 10;
				} while (dig_out > 10);
				System.out.print(dig_out + " ");
			}
			System.out.println();
			System.out.print(String.format("%-" + LABEL_LENGTH + "s\t", ""));
		}
		System.out.println();
		}
}
