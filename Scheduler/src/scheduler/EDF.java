package scheduler;

public class EDF extends Scheduler {
	@Override
	protected Task getTaskInstance (int exec, int period, int deadline, int absoluteDeadline, int releaseOffset,long id) {
		return new EDFTask(exec, period, deadline, absoluteDeadline, releaseOffset,id);
	}
	private class EDFTask extends Task implements Comparable{	

		public EDFTask(int exec, int period, int deadline, int absoluteDeadline ,int releaseOffset, long id) {		
			super(exec, period, deadline, absoluteDeadline,releaseOffset, id);
		}		
		@Override
		public int compareTo (Task compareTo) {
			int result = 0;
			if (this.absoluteDeadline < compareTo.absoluteDeadline) result = -1;
			else if (this.absoluteDeadline == compareTo.absoluteDeadline) {
				result = 0;//(this.id > task.id) ? 1 : -1;
			}
			else result = 1;
			return result + super.compareTo(compareTo); 
		}
	}
}
