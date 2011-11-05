
package RockManager.ui.screen.informScreen;

import net.rim.device.api.system.DeviceInfo;
import RockManager.config.Config;


public class AboutScreen extends InformScreen {

	protected String getTitle() {

		return "Rock File Manager";

	}


	protected void addMainArea() {

		addLabelField(Config.VERSION_NAME);
		addLabelField("Version: " + Config.VERSION_NUMBER);
		String osVersion = DeviceInfo.getDeviceName() + " " + DeviceInfo.getSoftwareVersion();
		addLabelField("OS: " + osVersion);
		addLabelField("Website: manager.rock-it.org");
		addLabelField("Contact: manager@rock-it.org");

	}

}
