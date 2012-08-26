
package RockManager.util.quickExit;

import java.util.Vector;


public class QuickExitRegistry {

	private static Vector QUICK_EXIT_SCREENS = new Vector();


	public static void addLog(QuickExitScreen screen) {

		if (QUICK_EXIT_SCREENS.contains(screen) == false) {
			QUICK_EXIT_SCREENS.addElement(screen);
		}
	}


	public static void removeLog(QuickExitScreen screen) {

		QUICK_EXIT_SCREENS.removeElement(screen);
	}


	public static void doAllCleanJob() {

		for (int i = 0; i < QUICK_EXIT_SCREENS.size(); i++) {
			QuickExitScreen screen = (QuickExitScreen) QUICK_EXIT_SCREENS.elementAt(i);
			screen.doCleanJob();
		}
	}

}
