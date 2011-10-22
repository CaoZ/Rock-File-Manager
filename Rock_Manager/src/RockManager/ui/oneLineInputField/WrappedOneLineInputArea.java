
package RockManager.ui.oneLineInputField;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;


/**
 * OneLineInputArea的一个包装，将OneLineInputArea放到了一个HorizontalFieldManager中。<br>
 * 因为有时若将OneLineInputArea放到黑色背景的manager中
 * ，在os6及以上系统中HorizonScroll可以为负值（用手指滑动等），这样就不可避免的需露出manager的背景色
 * 。即使对OneLineInputArea设置背景也不行，所以将OneLineInputArea放到这个HFM中，再对此HFM设置背景。
 */
public class WrappedOneLineInputArea extends HorizontalFieldManager {

	private OneLineInputArea inputArea;


	public WrappedOneLineInputArea(long style) {

		inputArea = new OneLineInputArea(style);
		add(inputArea);

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


	/**
	 * 是否为空。
	 * 
	 * @return
	 */
	public boolean isEmpty() {

		return inputArea.isEmpty();
	}


	/**
	 * 获得InputField.
	 * 
	 * @return
	 */
	public InputField getInputField() {

		return inputArea.getInputField();
	}


	/**
	 * 设置label.
	 * 
	 * @param label
	 */
	public void setLabelText(String label) {

		inputArea.setLabelText(label);

	}


	/**
	 * 设置文字。
	 * 
	 * @param text
	 */
	public void setText(String text) {

		inputArea.setText(text);

	}


	public int getPreferredHeight() {

		return inputArea.getPreferredHeight() + getMarginTop() + getMarginBottom() + getBorder().getTop()
				+ getBorder().getBottom() + getPaddingTop() + getPaddingBottom();

	}


	/**
	 * 设置固定宽度。
	 * 
	 * @param width
	 */
	public void setFixedWidth(int width) {

		inputArea.setFixedWidth(width);

	}


	public void replaceInputField(InputField inputField) {

		inputArea.replaceInputField(inputField);

	}

}
