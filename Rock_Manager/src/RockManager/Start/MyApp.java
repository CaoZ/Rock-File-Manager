
package RockManager.Start;

import RockManager.ui.MyUI;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.UiApplication;


public class MyApp extends UiApplication {

	public static void main(String[] args) {

		MyApp theApp = new MyApp();
		theApp.enterEventDispatcher();

	}


	public MyApp() {

		Locale.setDefault(Locale.get(Locale.LOCALE_zh_CN));
		FontManager.getInstance().setApplicationFont(MyUI.MAIN_FONT);
		pushScreen(new StartScreen());

	}

}
