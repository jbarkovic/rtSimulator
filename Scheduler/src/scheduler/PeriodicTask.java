package scheduler;
import java.util.ArrayList;

	public class PeriodicTask extends Task implements Comparable {
		private int period     = 0;
		private int deadline   = 0;
		private int execReq    = 0;
		
		private int execRemain = 0;
		
		public PeriodicTask(int[][] tasks) {
			super(tasks);
			period   = tasks [0][2];
			deadline = tasks [0][1];
			execReq  = tasks [0][0];			
		}
		
		@Override
		public int [] getTimeSlice () {
			
		}
		public int compareTo (Task task) {
			return 0;
		}
		@Override
		public int compareTo(Object o) {
			if (o instanceof Task) {
				return compareTo((Task) o);
			}
			return 0;
		}
	}