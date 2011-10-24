
package RockManager.util;

import net.rim.device.api.system.DeviceInfo;


public class OSVersionUtil {

	private static String[] VERSION_STRINGS;

	private static int MAIN_VERSION;

	static {
		VERSION_STRINGS = UtilCommon.splitString(DeviceInfo.getSoftwareVersion(), ".");
		try {
			MAIN_VERSION = Integer.parseInt(VERSION_STRINGS[0]);
		} catch (Exception e) {
			MAIN_VERSION = -1; // can't get
		}
	}


	public static boolean isOS5() {

		return isOS(5);
	}


	public static boolean isOS6() {

		return isOS(6);
	}


	public static boolean isOS(int mainVersion) {

		return MAIN_VERSION == mainVersion;
	}


	public static int getRevisionVersion() {

		try {
			return Integer.parseInt(VERSION_STRINGS[3]);
		} catch (Exception e) {
			return -1;
		}
	}

}
