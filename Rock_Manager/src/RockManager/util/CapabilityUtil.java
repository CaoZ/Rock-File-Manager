
package RockManager.util;

import net.rim.device.api.system.capability.DeviceCapability;


/**
 * 设备相关能力属性类。创建此类的目的是对OS5, OS6+设备提供相同的调用接口。
 */
public class CapabilityUtil {

	/**
	 * 此设备是否拥有物理键盘。
	 * 
	 * @return
	 */
	public static boolean isPhysicalKeyboardSupported() {

		return DeviceCapability.isPhysicalKeyboardSupported();
	}


	/**
	 * 物理键盘是否可用。
	 * 
	 * @return
	 */
	public static boolean isPhysicalKeyboardAvailable() {

		return DeviceCapability.isPhysicalKeyboardAvailable();
	}


	/**
	 * 是否可以使用虚拟键盘。如9800，在滑盖打开的情况下将返回false, 而在滑盖关闭的情况下降返回true.
	 * 
	 * @return
	 */
	public static boolean isVirtualKeyboardAllowed() {

		return DeviceCapability.isVirtualKeyboardAllowed();
	}

}
