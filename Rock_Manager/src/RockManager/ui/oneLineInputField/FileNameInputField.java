
package RockManager.ui.oneLineInputField;

import net.rim.device.api.ui.Keypad;


public class FileNameInputField extends InputField {

	/**
	 * 正处于自动选择模式。
	 */
	private boolean autoSelecting;

	public static final int LEVEL_NONE = 1;

	public static final int LEVEL_NAME_ONLY = 2;

	public static final int LEVEL_ALL = 3;

	private int autoSelectLevel = LEVEL_NONE;


	public void setAutoSelectLevel(int level) {

		if (level < LEVEL_NONE || level > LEVEL_ALL) {
			throw new IllegalArgumentException("Illegal AutoSelect Level!");
		}
		autoSelectLevel = level;
	}


	protected void onFocus(int direction) {

		super.onFocus(direction);

		if (autoSelectLevel == LEVEL_NONE) {
			return;
		}

		if (direction == 0 || isEmpty()) {
			// 如果direction为0，可能是由触摸、单击获得的焦点，不应执行下面的选中部分文字部分。否则就相当于选中部分文字后再单击文字了，这样会弹出快捷菜单（在os6上会出来九宫格式菜单，包括复制文字、剪切等）。
			// 执行顺序onFocus->touchEvent().
			// 即使是使用envokeLater过一会再选中文字有是也不能阻止弹出菜单的出现（可能与单击时touchGesture方向、时间有关）。
			return;
		}

		// select file name part
		int dotPosition = (autoSelectLevel == LEVEL_NAME_ONLY) ? getText().lastIndexOf('.') : getTextLength();

		if (dotPosition <= 0) {
			// select all
			dotPosition = getTextLength();
		}

		autoSelecting = true;

		setCursorPosition(0);
		select(true);
		setCursorPosition(dotPosition);

	}


	public void select(boolean enable) {

		if (autoSelecting && enable == false) {
			autoSelecting = false;
		}
		super.select(enable);

	}


	protected boolean navigationMovement(int dx, int dy, int status, int time) {

		if (autoSelecting && isSelecting()) {
			select(false);

			boolean onlyX = (dx != 0 && dy == 0);
			if (onlyX) {
				// 只有水平方向移动，光标不动。
				return true;
			}

			boolean moveDown = (dy > 0);
			if (moveDown) {
				int cursorPosition = getCursorPosition();
				int length = getTextLength();

				boolean alreadyAtEnd = cursorPosition == length;

				if (alreadyAtEnd) {
					// 向下方向移动并且光标已经在最后了，光标不动，不移出边框。
					return true;
				}
			}

		}

		return super.navigationMovement(dx, dy, status, time);

	}


	protected boolean trackwheelRoll(int amount, int status, int time) {

		// 在实际设备上这个方法不会被调用，因为滚轮早就取消了。但在模拟器中鼠标滚轮滚动时会调用此方法。

		if (autoSelecting && isSelecting()) {
			select(false);
		}

		return super.trackwheelRoll(amount, status, time);
	}


	public boolean keyChar(char key, int status, int time) {

		if (autoSelecting && key == Keypad.KEY_ESCAPE) {
			return false; // not consumed
		}
		return super.keyChar(key, status, time);
	}

}
