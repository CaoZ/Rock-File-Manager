package RockManager.timeLog;

public class TimeLog {

	private static long time;

	public static void reset() {
		time = System.currentTimeMillis();
	}

	public static void print() {
		print(null);
	}

	public static void print(String info) {
		System.out.println("========================");
		if (info == null) {
			info = "Time: ";
		} else {
			info = info + " time: ";
		}
		long now = System.currentTimeMillis();
		System.out.println(info + (now - time));
		System.out.println("------------------------");
		time = now;
	}

}
