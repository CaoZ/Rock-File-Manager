
package RockManager.util.ui;

import RockManager.ui.MyUI;
import RockManager.util.KeyUtil;
import RockManager.util.UtilCommon;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.util.MathUtilities;


public class BaseObjectListField extends ObjectListField {

	private static int BACK_COLOR_TOP = 0x088eef;

	private static int BACK_COLOR_BOTTOM = 0x1065de;

	private boolean isFocused;

	private Bitmap listFocusBack;


	public BaseObjectListField() {

		super();
	}


	public BaseObjectListField(long style) {

		super(style);
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

		if (isEmpty()) {
			return super.keyRepeat(keycode, time);
		}

		int keyChar = Keypad.key(keycode);
		int status = Keypad.status(keycode);
		switch (keyChar) {
			case Keypad.KEY_SPACE:
				moveFocus(1, status, time);
				return true;
		}
		return super.keyRepeat(keycode, time);

	}


	protected boolean keyChar(char key, int status, int time) {

		if (isEmpty()) {
			return super.keyChar(key, status, time);
		}

		switch (key) {

			case Keypad.KEY_SPACE:
				if (getSelectedIndex() == getSize() - 1) {
					// 到达末尾后移动到开头。
					setSelectedIndex(0);
				} else {
					moveFocus(1, status, time);
				}
				return true;

			case Keypad.KEY_ENTER:
				boolean consumed = navigationClick(status, time);
				if (consumed) {
					return true;
				}
				break;

		}

		boolean altDown = KeyUtil.isAltPressed(status);

		if (altDown && getSize() > 1) {

			char unAltedKey = Character.toUpperCase(Keypad.getUnaltedChar(key));

			if (unAltedKey == 'T') {
				// 跳转到顶部
				setSelectedIndex(0);
				return true;
			} else if (unAltedKey == 'B') {
				// 跳转到底部
				setSelectedIndex(getSize() - 1);
				return true;
			}

		}

		return super.keyChar(key, status, time);

	}


	/**
	 * moveFocus 重载此方法是为了能按空格键向下移动。
	 */
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


	protected void layout(int width, int height) {

		int oldWidth = getWidth();
		super.layout(width, height);
		int newWidth = getWidth();

		if (oldWidth != newWidth) {
			// 两次宽度不同，可能刚初始化或屏幕方向发生了变化，或extend发生了变化，需重新生成listFocusBack。
			listFocusBack = null;
		}

	}


	/**
	 * 对每行进行绘制。（仅背景）
	 */
	public void drawListRow(ListField listField, Graphics g, int index, int y, int width) {

		// 绘制高亮项的背景
		if (isFocused && getSelectedIndex() == index) {
			drawListFocusBack(g, y, width, getRowHeight());
		}

	}


	private void drawListFocusBack(Graphics g, int y, int width, int rowHeight) {

		int paddingX = 1;
		int paddingY = 1;
		// 若是屏幕方向旋转了，listFocusBack会重置为null，需重新生成listFocusBack
		if (listFocusBack == null) {
			createListFocusBack(width - 2 * paddingX, rowHeight - 2 * paddingY);
		}
		g.drawBitmap(paddingX, y + paddingY, listFocusBack.getWidth(), listFocusBack.getHeight(), listFocusBack, 0, 0);
	}


	private void createListFocusBack(int width, int height) {

		listFocusBack = new Bitmap(width, height);
		Graphics g = Graphics.create(listFocusBack);
		int[] xPts = { 0, width, width, 0 };
		int[] yPts = { 0, 0, height, height };
		int[] colors = { BACK_COLOR_TOP, BACK_COLOR_TOP, BACK_COLOR_BOTTOM, BACK_COLOR_BOTTOM };
		g.drawShadedFilledPath(xPts, yPts, null, colors, null);

		// 根据字体大小设置设置适当的圆边半径。
		int alphaMaskR = MyUI.deriveSize(7);
		// 可用圆边范围：4-9。
		alphaMaskR = MathUtilities.clamp(4, alphaMaskR, 9);

		String alphaMask_path = "img/other/alphaMask_{1}.png";
		alphaMask_path = UtilCommon.replaceString(alphaMask_path, "{1}", Integer.toString(alphaMaskR));
		Bitmap alphaMask = Bitmap.getBitmapResource(alphaMask_path);

		int arcSize = alphaMask.getWidth() / 2;
		int[] backData = new int[arcSize * arcSize];
		int[] alphaData = new int[arcSize * arcSize];

		// back Bitmap四个将要被替换的角的左上角开始位置。
		int[] backXs = { 0, width - arcSize, 0, width - arcSize };
		int[] backYs = { 0, 0, height - arcSize, height - arcSize };
		// 透明mask的四个角的左上角开始位置。
		int[] maskXs = { 0, arcSize, 0, arcSize };
		int[] maskYs = { 0, 0, arcSize, arcSize };

		for (int index = 0; index < backXs.length; index++) {

			listFocusBack.getARGB(backData, 0, arcSize, backXs[index], backYs[index], arcSize, arcSize);
			alphaMask.getARGB(alphaData, 0, arcSize, maskXs[index], maskYs[index], arcSize, arcSize);

			for (int i = arcSize * arcSize - 1; i >= 0; i--) {
				// 取得alpha的前8位（alpha数据），与icon相混合
				backData[i] = (alphaData[i] & 0xff000000) | (backData[i] & 0x00ffffff);
			}

			listFocusBack.setARGB(backData, 0, arcSize, backXs[index], backYs[index], arcSize, arcSize);
		}

	}

}