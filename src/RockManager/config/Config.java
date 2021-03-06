
package RockManager.config;

import net.rim.device.api.system.DeviceInfo;


public class Config {

	public static final boolean DEBUG_MODE;

	public static final String APP_NAME = "Rock File Manager for OS 6+";

	public static final String VERSION_NUMBER = "1.0.1";

	static {

		int pin = DeviceInfo.getDeviceId();

		if (DeviceInfo.isSimulator() && pin == 0x2100000C) {
			DEBUG_MODE = true;
		} else {
			DEBUG_MODE = false;
		}

	}

}
