
package RockManager.ui.oneLineInputField;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;


/**
 * 单行文本输入框。
 */
public class OneLineInputArea extends HorizontalFieldManager {

	private InputField inputField;

	private int minWidth = -1;

	private int maxWidth = -1;

	private int fixedWidth = -1;


	public OneLineInputArea(long style) {

		super(style | HORIZONTAL_SCROLL);
		inputField = new InputField();
		add(inputField);

	}


	protected void sublayout(int width, int height) {

		if (fixedWidth > 0) {
			setExtent(fixedWidth, getHeight());
			width = fixedWidth;
		}

		setExtent(width, height); // 暂时设置宽度和高度，子field中可能要用到这些值。这些值是可以在super.sublayout()中被覆盖的。
		super.sublayout(width, height);

		// 在需要滚动时滚动到最右边，使之右对齐
		setHorizontalScroll(Integer.MAX_VALUE, false);

	}


	/**
	 * 是否为空。
	 * 
	 * @return
	 */

	public boolean isEmpty() {

		return inputField.isEmpty();
	}


	/**
	 * 获得InputField.
	 * 
	 * @return
	 */
	public InputField getInputField() {

		return inputField;
	}


	/**
	 * 设置label.
	 * 
	 * @param label
	 */
	public void setLabelText(String label) {

		inputField.setLabelText(label);

	}


	/**
	 * 设置文字。
	 * 
	 * @param text
	 */
	public void setText(String text) {

		inputField.setText(text);

	}


	public int getPreferredHeight() {

		return inputField.getPreferredHeight() + getMarginTop() + getMarginBottom()
				+ getBorder().getTop() + getBorder().getBottom() + getPaddingTop()
				+ getPaddingBottom();

	}


	/**
	 * 设置固定宽度。
	 * 
	 * @param width
	 */
	public void setFixedWidth(int width) {

		fixedWidth = width;

	}


	/**
	 * 激活默认样式Border。
	 */
	public void activeDefaultBorder() {

		XYEdges edges = new XYEdges(11, 9, 10, 9);
		Bitmap bitmap = Bitmap.getBitmapResource("img/other/inputBack.png");
		Border border = BorderFactory.createBitmapBorder(edges, bitmap);
		setBorder(border);

	}


	public void replaceInputField(InputField newInputField) {

		replace(inputField, newInputField);
		inputField = newInputField;

	}

}
