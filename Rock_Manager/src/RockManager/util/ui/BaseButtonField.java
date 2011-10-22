
package RockManager.util.ui;

import java.util.Timer;
import java.util.TimerTask;
import RockManager.util.KeyUtil;
import net.rim.device.api.system.capability.DeviceCapability;
import net.rim.device.api.system.capability.DeviceCapabilityListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;


/**
 * 对ButtonField的扩充，可在外部进行触发。
 */
public class BaseButtonField extends ButtonField implements DeviceCapabilityListener {

	private static int MIN_CLICK_INTERVAL = 100;

	private long lastClickTime;

	private String labelWithShortcut;

	private String label;


	public BaseButtonField(String label) {

		super(label);
	}


	public BaseButtonField(String label, long style) {

		super(label, style);
	}


	public void activeTextChangeMode() {

		DeviceCapability.addPhysicalKeyboardListener(this);
		// CZTODO 这个listener不卸载会造成怎样的后果？
		resetLabel();

	}


	/**
	 * 模拟单击或触摸的触发。
	 */
	public void click() {

		boolean wait = false;

		if (isFocus() == false && isVisible() && getScreen() != null) {

			setFocus();
			wait = true;

		}

		if (wait) {
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {

				public void run() {

					// 在event thread中运行，因为可能涉及操作界面的活动，如关闭一个screen。
					UiApplication.getUiApplication().invokeAndWait(new Runnable() {

						public void run() {

							invokeAction(Field.ACTION_INVOKE);

						}
					});

				}
			};
			// 等待0.1s,让button获得焦点并绘制，就好像是手工点击了一样。
			timer.schedule(task, MIN_CLICK_INTERVAL);
		} else {
			invokeAction(Field.ACTION_INVOKE);
		}

	}


	protected void fieldChangeNotify(int context) {

		long now = System.currentTimeMillis();
		if (isValidClick(now)) {
			super.fieldChangeNotify(context);
			lastClickTime = now;
		}

	}


	/**
	 * 是否是有效的点击。因为要先使按钮setFocus(), 等一会再触发，所以要防止短时间内（0.1s左右）多次按下按钮导致的错误的多次触发。
	 * 
	 * @param now
	 * @return
	 */
	protected boolean isValidClick(long now) {

		// 使用绝对值。系统时间可能是不可靠的，例如，用户调整了系统时间后。
		if (Math.abs(now - lastClickTime) > MIN_CLICK_INTERVAL) {
			return true;
		} else {
			return false;
		}

	}


	public void setLabel(String labelWithShortcut, String label) {

		this.labelWithShortcut = labelWithShortcut;
		this.label = label;
	}


	private void resetLabel() {

		if (KeyUtil.isPhysicalKeyboardAvailable()) {
			setLabel(labelWithShortcut);
		} else {
			setLabel(label);
		}
	}


	public void allowedChanged(boolean changedTo) {

	}


	public void availableChanged(boolean changedTo) {

		resetLabel();
	}


	public void supportedChanged(boolean changedTo) {

	}

}
