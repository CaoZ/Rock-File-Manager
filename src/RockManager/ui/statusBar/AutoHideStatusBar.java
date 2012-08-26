
package RockManager.ui.statusBar;

import java.util.Timer;
import java.util.TimerTask;
import RockManager.util.ui.VFMwithScrollbarControl;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;


public class AutoHideStatusBar extends StatusBar {

	private int autoHideTime;


	/**
	 * 自动隐藏的信息栏。
	 * 
	 * @param info
	 * @param hideTime
	 *            多长时间后自动隐藏，毫秒。
	 */
	public AutoHideStatusBar(String info, int hideTime) {

		super(info);
		autoHideTime = hideTime;

	}


	/**
	 * 添加到某一Screen并显示。
	 * 
	 * @param parentScreen
	 */
	public void addTo(final MainScreen parentScreen) {

		final StatusBar thisStatusBar = this;

		if (parentScreen instanceof VFMwithScrollbarControl) {
			((VFMwithScrollbarControl) parentScreen).setScrollBarAutoShow(false);
		}
		parentScreen.setStatus(thisStatusBar);

		Timer hideTimer = new Timer();
		TimerTask hideTask = new TimerTask() {

			public void run() {

				if (thisStatusBar.getScreen() != parentScreen) {
					return;
				}

				UiApplication.getUiApplication().invokeAndWait(new Runnable() {

					public void run() {

						parentScreen.setStatus(null);
					}
				});

				if (parentScreen instanceof VFMwithScrollbarControl) {
					((VFMwithScrollbarControl) parentScreen).setScrollBarAutoShow(true);
				}

			}
		};

		hideTimer.schedule(hideTask, autoHideTime);

	}

}
