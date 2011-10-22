
package RockManager.util;

import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.system.capability.DeviceCapability;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.KeypadUtil;


public class KeyUtil {

	/**
	 * 检测按下的键和要测试的键是否在一颗物理按键上(多用于快捷键检测)。
	 * 
	 * @param keyPressed
	 * @param status
	 * @param keyToTest
	 * @return
	 */
	public static boolean isOnSameKey(char keyPressed, int status, char keyToTest) {

		if (isPhysicalKeyboardAvailable() == false) {
			// 物理键盘不可用时返回false，禁用快捷键。
			return false;
		}

		keyToTest = Character.toLowerCase(keyToTest);

		if (Character.toLowerCase(keyPressed) == keyToTest) {
			return true;
		}

		StringBuffer allChars = new StringBuffer();

		if (isAltPressed(status)) {
			char originKey = Keypad.getUnaltedChar(keyPressed);
			int keyCode = KeypadUtil.getKeyCode(originKey, 0, KeypadUtil.MODE_UI_CURRENT_LOCALE);
			Keypad.getKeyChars(keyCode, allChars);
			allChars.append(keyPressed);
		} else {
			int keyCode = KeypadUtil.getKeyCode(keyPressed, status,
					KeypadUtil.MODE_UI_CURRENT_LOCALE);
			Keypad.getKeyChars(keyCode, allChars);
			allChars.append(Keypad.getAltedChar(keyPressed));
		}

		String allCharsString = allChars.toString().toLowerCase();

		return allCharsString.indexOf(keyToTest) >= 0;

	}


	/**
	 * alt键是否按下了。
	 * 
	 * @param status
	 * @return
	 */
	public static boolean isAltPressed(int status) {

		return (KeypadListener.STATUS_ALT & status) == KeypadListener.STATUS_ALT;

	}


	/**
	 * 获取物理键盘上的显著按键。如在9105上，7、pqrs共用一个按键，7是显著地。
	 * 
	 * @param origin
	 * @return
	 */
	public static char getDistinctKey(char originKey) {

		StringBuffer allChars = new StringBuffer();
		int keyCode = KeypadUtil.getKeyCode(Character.toLowerCase(originKey), 0,
				KeypadUtil.MODE_UI_CURRENT_LOCALE);
		Keypad.getKeyChars(keyCode, allChars);
		if (allChars.length() >= 2) {
			// 该字母所在的按键上不止这一个字母，如sureType型键盘。
			return Keypad.getAltedChar(originKey);
		} else {
			// 这个字母是显著地，如QWERTY键盘上的字母，或是相当于alt按下时的数字键等。
			return originKey;
		}

	}


	/**
	 * 物理键盘是否可用。
	 * 
	 * @return
	 */
	public static boolean isPhysicalKeyboardAvailable() {

		return DeviceCapability.isPhysicalKeyboardAvailable();

	}

}
