package scheduler;
public class RM extends Scheduler {
	@Override
	protected Task getTaskInstance (int exec, int period, int deadline, int absoluteDeadline, int releaseOffset,long id) {
		return new RMTask(exec, period, deadline, absoluteDeadline, releaseOffset,id);
	}
	private class RMTask extends Task implements Comparable{	

		public RMTask(int exec, int period, int deadline, int absoluteDeadline ,int releaseOffset, long id) {		
			super(exec, period, deadline, absoluteDeadline, releaseOffset, id);
		}
		@Override
		public int compareTo (Task compareTo) {	
			//System.out.println("USING RM PRIORITIES");
			int result = 0;
			if (Math.abs(this.period) < Math.abs(compareTo.period)) result = -1;
			else if (Math.abs(this.period) == Math.abs(compareTo.period)) {
				result = 0;//(this.id > task.id) ? 1 : -1;
			}
			else result = 1;
			return result + super.compareTo(compareTo); 
		}
	}
}