
package RockManager.util;

import RockManager.languages.LangRes;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.component.Menu;


public class FixUtil {

	public static void fixVirtualKeyboardMenuItem(Menu menu, Screen screen) {

		boolean virtualKeyboardAllowed = CapabilityUtil.isVirtualKeyboardAllowed();
		boolean noSystemMenu = screen.isStyle(Screen.NO_SYSTEM_MENU_ITEMS);

		if (!virtualKeyboardAllowed || !noSystemMenu) {
			// no need to add
			return;
		}

		// 系统的isVirtualKeyboardAvailable()总是返回false, 所以不能使用。
		final VirtualKeyboard vk = screen.getVirtualKeyboard();
		if (vk == null) {
			return;
		}

		int visibility = vk.getVisibility();
		boolean isShown = (visibility == VirtualKeyboard.SHOW || visibility == VirtualKeyboard.SHOW_FORCE);
		boolean isHiden = (visibility == VirtualKeyboard.HIDE || visibility == VirtualKeyboard.HIDE_FORCE);

		if (!isShown && !isHiden) {
			// 既不是显示，也不是不显示，是啥状态呢？未知状态，返回。虽然几乎不可能出现这种情况。
			return;
		}

		MenuItem vkItem;
		String text;
		int ordinal = 20480;
		int priority = 2147483647;

		if (isShown) {
			// virtual keyboard已显示，添加隐藏菜单。
			text = LangRes.getString(LangRes.MENU_VIRTUAL_KEYBOARD_HIDE);
			vkItem = new MenuItem(text, ordinal, priority) {

				public void run() {

					vk.setVisibility(VirtualKeyboard.HIDE);
				}
			};

		} else {
			// virtual keyboard未显示，添加呼出菜单。
			text = LangRes.getString(LangRes.MENU_VIRTUAL_KEYBOARD_SHOW);
			vkItem = new MenuItem(text, ordinal, priority) {

				public void run() {

					vk.setVisibility(VirtualKeyboard.SHOW);
				}
			};
		}

		menu.add(vkItem);

	}
}
