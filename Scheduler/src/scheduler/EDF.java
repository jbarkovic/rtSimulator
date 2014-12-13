package scheduler;

public class EDF extends Schedule {
	@Override
	public int compare (Task t1, Task t2) {
		if (! (t1 instanceof PeriodicTask) || ! (t2 instanceof PeriodicTask)) {
			return 0;
		}
		else {
			PeriodicTask pt1 = (PeriodicTask) t1;
			PeriodicTask pt2 = (PeriodicTask) t2;
			if (pt1.getAbsoluteDeadline() < pt2.getAbsoluteDeadline()) return -1;
			else if (pt1.getAbsoluteDeadline() == pt2.getAbsoluteDeadline()) return (pt1.hasRunYet()) ? -1 : 1;
			else return 1;						
		}
	}
}
