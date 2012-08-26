
package RockManager.util.ui;

import net.rim.device.api.ui.Field;


public class MyStyle {

	/**
	 * 不使用全部宽度，CheckBoxField.NO_USE_ALL_WIDTH, 直到OS7才公开，但在OS5已可用。
	 */
	public static final long NO_USE_ALL_WIDTH = 2147483648L;

	/**
	 * Field.RIGHT_TO_LEFT.
	 */
	public static final long RIGHT_TO_LEFT = 0x8000000;

	/**
	 * 自定义Style, Field.USE_ALL_WIDTH | RIGHT_TO_LEFT, 可使CheckBox的label在前，box在最右。
	 */
	public static final long LABEL_BOX_CHECKBOX = Field.USE_ALL_WIDTH | RIGHT_TO_LEFT;

}
