
package RockManager.util.ui;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;


/**
 * 像Button一样可以点击的Field.
 */
public abstract class BaseButtonLikeField extends Field {

	public BaseButtonLikeField() {

		this(0);
	}


	public BaseButtonLikeField(long style) {

		super(Field.FOCUSABLE | style);
	}


	protected boolean keyChar(char character, int status, int time) {

		if (character == Characters.ENTER) {
			clickButton();
			return true;
		}
		return super.keyChar(character, status, time);
	}


	protected boolean navigationClick(int status, int time) {

		clickButton();
		return true;
	}


	protected boolean trackwheelClick(int status, int time) {

		clickButton();
		return true;
	}


	protected boolean invokeAction(int action) {

		switch (action) {
			case ACTION_INVOKE: {
				clickButton();
				return true;
			}
		}
		return super.invokeAction(action);
	}


	public void setDirty(boolean dirty) {

		// We never want to be dirty or muddy
	}


	public void setMuddy(boolean muddy) {

		// We never want to be dirty or muddy
	}


	/**
	 * A public way to click this button
	 */
	public void clickButton() {

		fieldChangeNotify(0);
	}
}
