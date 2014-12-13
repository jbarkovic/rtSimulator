package scheduler;

	public class Task implements Comparable {
		protected int executionRemaining = 0;
		protected int TOTAL_EXECUTION; 
		protected int index = 0;
		public Task (int execution, int index) {
			this (execution);
			this.index = index;
		}
		public Task (int execution) {
			this.TOTAL_EXECUTION = execution;
			this.executionRemaining = this.TOTAL_EXECUTION;
		}
		public boolean hasRunYet () {
			return this.executionRemaining < this.TOTAL_EXECUTION;
		}
		public RunResult [] step (long timeInstance) {
			RunResult [] retList = new RunResult [1];
			if (this.executionRemaining > 0) {
				this.executionRemaining--;
				retList [0] = RunResult.EXECUTED;
				if (this.executionRemaining == 0) retList = new RunResult [] {RunResult.EXECUTED,RunResult.TASK_COMPLETED};
			} else {
				retList [0] = RunResult.UNABLE_TO_RUN;
			}
			return retList;
		}
		public int getExecutionRemaining () {
			return this.executionRemaining;
		}
		public int getIndex () {
			return this.index;
		}
		public void setIndex (int index) {
			this.index = index;
		}
		public int getTotalExecution () {
			return this.TOTAL_EXECUTION;
		}
		public boolean isDone () {
			return this.executionRemaining <= 0;
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