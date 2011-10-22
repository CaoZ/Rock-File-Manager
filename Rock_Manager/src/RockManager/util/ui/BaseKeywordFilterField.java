package RockManager.util.ui;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.KeywordFilterField;

public class BaseKeywordFilterField extends KeywordFilterField {

	private boolean drawLine = false;
	public static int lineColorTop = 0xffffff;
	private static int lineColorBottom = 0xcecbce;
	private static int backColorTop = 0x088eef;
	private static int backColorBottom = 0x1065de;
	private boolean isFocused;
	private Bitmap listFocusBack;

	/**
	 * 是否绘制上下的两条线，效果如同笔记本纸上的线，如果每一屏幕显示的行数较多，绘制线条较好看。
	 */
	public void setDrawLine(boolean value) {
		drawLine = value;
	}

	protected void drawFocus(Graphics g, boolean on) {
		// do nothing
	}

	protected void onFocus(int direction) {
		isFocused = true;
		super.onFocus(direction);
	}

	protected void onUnfocus() {
		isFocused = false;
		super.onUnfocus();
	}

	/**
	 * 返回此ListField是否在focused状态。
	 * 
	 * @return
	 */
	public boolean isFocused() {
		return isFocused;
	}

	/**
	 * 返回此行是否在focused状态。
	 * 
	 * @param index
	 * @return
	 */
	public boolean isFocused(int index) {
		return isFocused && (getSelectedIndex() == index);
	}

	protected boolean keyRepeat(int keycode, int time) {

		if (getSize() < 1) {
			return super.keyRepeat(keycode, time);
		}

		int keyChar = Keypad.key(keycode);
		int status = Keypad.status(keycode);
		switch (keyChar) {
			case Keypad.KEY_SPACE:
				// 输入内容为空时，按住空格键一直向下滚动
				if (getKeyword().length() == 0) {
					moveFocus(1, status, time);
					return true;
				}
				break;
		}
		return super.keyRepeat(keycode, time);

	}

	protected boolean keyChar(char key, int status, int time) {

		if (getSize() < 1) {
			return super.keyChar(key, status, time);
		}

		switch (key) {
			case Keypad.KEY_SPACE:
				// 输入内容为空时，按空格键向下滚动。
				if (getKeyword().length() == 0) {
					moveFocus(1, status, time);
					return true;
				}
				break;
			case Keypad.KEY_ENTER:
				navigationClick(status, time);
				return true;
		}
		return super.keyChar(key, status, time);
	}

	// moveFocus 重载此方法是为了能按空格键向下移动。
	protected int moveFocus(int amount, int status, int time) {

		int number;
		if (Math.abs(amount) > 1) {
			number = amount;
		} else {
			boolean multi = ((status & KeypadListener.STATUS_ALT) == KeypadListener.STATUS_ALT);
			number = multi ? 5 : 1;
			if (amount < 0) {
				number = -number;
			}
		}

		int target = getSelectedIndex() + number;
		if (target < 0 || target >= getSize()) {
			return super.moveFocus(amount, status, time);
		} else {
			setSelectedIndex(target);
			return 0;
		}

	}

}