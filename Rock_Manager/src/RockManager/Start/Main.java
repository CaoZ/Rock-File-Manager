package RockManager.Start;

import net.rim.device.api.ui.UiApplication;

public class Main extends UiApplication {

	public static void main(String[] args) {
		Main theApp = new Main();
		theApp.enterEventDispatcher();
	}

	public Main() {
		pushScreen(new StartScreen());
	}
}
