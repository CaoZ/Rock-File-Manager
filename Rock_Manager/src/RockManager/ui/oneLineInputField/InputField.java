
package RockManager.ui.oneLineInputField;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.TextField;


/**
 * 在OneLineInputArea中的实际的TextField.
 */
public class InputField extends TextField {

	private String label;

	public static final int ENTER_PRESSED = "ENTER_PRESSED".hashCode();


	public InputField() {

		super(NO_EDIT_MODE_INPUT | NO_LEARNING | NON_SPELLCHECKABLE | NO_NEWLINE | JUMP_FOCUS_AT_END);
	}


	protected void fieldChangeNotify(int context) {

		// 输入文字后调整组件的宽度。
		if (context != ENTER_PRESSED) {
			updateLayout();
		}
		super.fieldChangeNotify(context);
	}


	public String getLabel() {

		return label;
	}


	/**
	 * 输入的文字的宽度，或提示文字的宽度。
	 * 
	 * @return
	 */
	public int getTextWidth() {

		// 获取文字的宽度，若是输入了文字，需加上光标的宽度，光标的宽度与一个空格的宽度相同。
		// 为空时即使不应显示label也以label宽度为准，因为此时若失去焦点造成重绘，不会重新计算宽度的，若仍以空格为准，则不会显示完整。
		String words = isEmpty() ? getLabel() : getText() + " ";
		int wordsWidth = getFont().getBounds(words);
		return wordsWidth;
	}


	/**
	 * 内容是否为空。
	 * 
	 * @return
	 */
	public boolean isEmpty() {

		return getTextLength() == 0;
	}


	protected void onFocus(int direction) {

		super.onFocus(direction);

		if (direction > 0 && !isSelecting()) {
			setCursorPosition(getTextLength());
		}

	}


	public boolean keyChar(char key, int status, int time) {

		if ((key == ' ') && isEmpty()) {
			// 以空格开头是无意义的，禁止以空格开头。
			return true;
		} else if (key == Keypad.KEY_ENTER) {
			fieldChangeNotify(ENTER_PRESSED);
			return true;
		}

		return super.keyChar(key, status, time);

	}


	protected void layout(int width, int height) {

		Manager manager = getManager();

		// 计算所需宽度
		// 如果它的manager的manager动态的添加了某个field，
		// 再重新layout的过程中可能会出现manager的ContentWidth反而比width大情况,
		// 如SearchBox中添加ClearIcon时。
		// 另外，若manager有边框时也应使用ContentWidth.
		width = Math.max(getTextWidth(), Math.min(manager.getContentWidth(), manager.getWidth()));

		super.layout(width, height);

		// 在需要滚动时滚动到最右边，使之右对齐。
		// 虽然在它的manager中的sublayout()也有同样的滚动语句，但是manager不一定需要relayout,
		// 所以此处的滚动语句也是不可少的。
		manager.setHorizontalScroll(Integer.MAX_VALUE, false);

	}


	protected void paint(Graphics g) {

		if ((isFocus() == false) && isEmpty()) {
			// 若用户没有输入内容，绘制提示文字，使用较浅颜色
			if (label != null && label.length() > 0) {
				g.setColor(0x888888);
				g.drawText(label, 0, 0);
			}
			return;
		}

		// 处于选择模式时设置文字为白色，正常时为黑色。
		boolean selectedMode = g.isDrawingStyleSet(Graphics.DRAWSTYLE_FOCUS)
				|| g.isDrawingStyleSet(Graphics.DRAWSTYLE_SELECT);
		g.setColor(selectedMode ? 0xffffff : 0x333333);
		super.paint(g);

	}


	public void setLabelText(String newLabel) {

		if (newLabel.equals(label) == false) {
			label = newLabel;
			invalidate();
		}

	}

}
