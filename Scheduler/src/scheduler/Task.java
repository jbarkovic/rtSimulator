package scheduler;
import java.util.ArrayList;

	public class Task implements Comparable {
		int period;
		ArrayList<Resource> resourceInfo = new ArrayList<Resource> ();
		int exec;
		boolean weAreAsync = false;
		boolean weJustRan = false;
		// Async Task data
		static int AsyncTotalRemain = 0;
		static int AsyncExecRemain = 0;
		
		protected int execRemain;
		protected int releaseOffset = 0;
		boolean done = false;
		boolean SIGQUIT = false;
		protected int absoluteDeadline = 0;
		long id = 0;
		public Task (int exec, int period, int deadline, int absoluteDeadline,int releaseOffset, long id) {		
			this.absoluteDeadline = absoluteDeadline;
			this.exec = exec;
			this.period = period;
//			this.deadline = deadline;
			this.execRemain = exec;
			this.done = false;
			this.id = id;
			this.releaseOffset = releaseOffset;
			
			if (period <= 0) weAreAsync = true;
			else weAreAsync = false;
			// Async Task data
			if (period < 0) {
				
				AsyncTotalRemain += exec;
				System.out.println("Adding Asyncronous task: " + exec);	
			} else if (period == 0) {
				System.out.println("Deffereable Server Period Change: " + exec);
				//AsyncExecRemain = Scheduler.defferableServerExecBuget;
				
			}
		}
		protected void step (boolean areWeRunning) { // 0 if nothing -1 if deadline missed 1 if done exec
			if (this.period > 0) {
				if (areWeRunning) {
					execRemain = Math.max(0, execRemain - 1);
				}			
				if (execRemain == 0) done = true;
			} else {
				if (areWeRunning) {
					AsyncTotalRemain = Math.max(0, AsyncTotalRemain - 1);
					AsyncExecRemain = Math.max(0, AsyncExecRemain - 1);	
					execRemain = Math.max(0, execRemain - 1);
				}
				if (AsyncTotalRemain == 0 || AsyncExecRemain == 0 || execRemain == 0) done = true;
				System.out.println("AsyncTotalRemain: " + AsyncTotalRemain + ", AsyncExecRemain: " + AsyncExecRemain);	
			}
			if (execRemain == 0 && period <= 0) SIGQUIT = true;
			if (areWeRunning) {
				weJustRan = true;
			} else weJustRan = false;
		}		
		public int compareTo (Task task) {
			if (Scheduler.deferrableServerHasHighestPriority) {
				if (this.weAreAsync && !task.weAreAsync) return -2;
				else if (this.weAreAsync && task.weAreAsync) return 0;
				else if (!this.weAreAsync && !task.weAreAsync) return 0;
				else return 2;
			} else {
				return 0;
			}
		}
		@Override
		public int compareTo(Object o) {
			if (o instanceof Task) {
				return compareTo((Task) o);
			}
			return 0;
		}
		private class Resource {
			int priorityCeiling = 0;			
			int locked = 0;
		}
	}