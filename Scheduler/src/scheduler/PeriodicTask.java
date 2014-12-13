package scheduler;

	public class PeriodicTask extends Task implements Comparable {
		private final int period;
		private final int deadline;	
		private final int started_at;
		
		public PeriodicTask(int execution, int period, int deadline, int dispatchedTime, int index) {
			super (execution,index);
			this.started_at = dispatchedTime;
			this.period   = period;
			this.deadline = deadline;			
		}
		public int getPeriod () {
			return this.period;
		}
		public int getDeadline () {
			return this.deadline;
		}
		public int getAbsoluteDeadline () {
			return this.started_at + this.deadline;
		}
		public int compareTo (PeriodicTask task) {
			if (this.period == task.period) return 0;
			else return (this.period > task.period) ? 1 : -1;
		}
		@Override
		public int compareTo(Object o) {
			if (o instanceof PeriodicTask) {
				return compareTo((PeriodicTask) o);
			}
			return 0;
		}
	}