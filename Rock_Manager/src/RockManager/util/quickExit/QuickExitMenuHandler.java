
package RockManager.util.quickExit;

import RockManager.languages.LangRes;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;


public class QuickExitMenuHandler {

	public static void handleExitMenuItem(Menu menu) {

		int screenCount = UiApplication.getUiApplication().getScreenCount();
		if (screenCount <= 1) {
			// 只有一个Screen，将Close换成Exit.
			changeCloseToExit(menu);
		} else {
			// 多个Screen，在最后添加Exit.
			addExitMenuItem(menu);
		}

	}


	private static void addExitMenuItem(Menu menu) {

		MenuItem closeItem = MenuItem.getPrefab(MenuItem.CLOSE);

		String text = LangRes.get(LangRes.MENU_EXIT);
		int ordinal = closeItem.getOrdinal() + 1;
		int priority = closeItem.getPriority() + 1;

		MenuItem exitItem = new MenuItem(text, ordinal, priority) {

			public void run() {

				QuickExitRegistry.doAllCleanJob();
				System.exit(0);
			}
		};
		menu.add(exitItem);

	}


	private static void changeCloseToExit(Menu menu) {

		MenuItem closeItem = MenuItem.getPrefab(MenuItem.CLOSE);

		int menuSize = menu.getSize();
		for (int i = menuSize - 1; i >= 0; i--) {
			if (menu.getItem(i).equals(closeItem)) {
				menu.deleteItem(i);
				break;
			}
		}

		addExitMenuItem(menu);

	}

}
